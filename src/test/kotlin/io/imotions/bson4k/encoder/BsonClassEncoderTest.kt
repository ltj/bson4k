/*
 * Copyright 2021 iMotions A/S
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.imotions.bson4k.encoder

import io.imotions.bson4k.Bson
import io.imotions.bson4k.common.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import org.bson.BsonInt32
import org.bson.BsonNull
import org.bson.BsonString

@Serializable
private data class DataClassWithDefaults(
    val s: String,
    val b: Boolean = true,
    val i: Int = 42
)

@ExperimentalSerializationApi
class BsonClassEncoderTest : StringSpec({
    "Encode class" {
        val clazz = Class2("Fred", 42)
        val document = bson.encodeToBsonDocument(clazz)
            .also { println(it.toJson()) }

        document.size shouldBeExactly 2
        document.keys shouldContainAll listOf("x", "y")
        document.getString("x") shouldBe BsonString(clazz.x)
        document.getInt32("y") shouldBe BsonInt32(clazz.y)
    }

    "Encode class with nested class" {
        val nested = Class2("Fred", 42)
        val clazz = Class2(1L, nested)

        val document = bson.encodeToBsonDocument(clazz)
            .also { println(it.toJson()) }

        document.keys shouldContainAll listOf("x", "y")
        document.getDocument("y").keys shouldContainAll listOf("x", "y")
    }

    "Encode multiple nested classe including embedded array" {
        val nested = Class2("Fred", 42)
        val clazz = Class3(1L, "This is a comment", nested)
        val set = Class2("This is content", setOf(clazz))

        val document = bson.encodeToBsonDocument(set)
            .also { println(it.toJson()) }

        document.keys shouldContainAll listOf("x", "y")
        document.getArray("y").size shouldBeExactly 1
        document.getArray("y")[0].isDocument shouldBe true
    }

    "Encode nullable fields" {
        val testClass = Class3<Long, Int?, Class2<String, Int>>(12L, null, Class2("xyz", -42))
        val document = bson.encodeToBsonDocument(testClass)
            .also { println(it.toJson()) }

        document["y"] shouldBe BsonNull()
    }

    "Encode enum fields" {
        val clazz = Class2<Byte, EnumClass>(16, EnumClass.THIRD)
        val document = bson.encodeToBsonDocument(clazz)
            .also { println(it) }

        document["y"] shouldBe BsonString(EnumClass.THIRD.name)
    }

    "Encode class with all null values" {
        val clazz = Wrapper2<Int?, Int?>(null, null)
        val document = bson.encodeToBsonDocument(clazz)
            .also { println(it) }

        document["x"] shouldBe BsonNull()
        document["y"] shouldBe BsonNull()
    }

    "Encode class with default nullables" {
        val encodeDefaultsBson = Bson {
            encodeDefaults = true
        }
        val clazz = Wrapper2Null<String, String>(y = "hello")
        val document = encodeDefaultsBson.encodeToBsonDocument(clazz)
            .also { println(it) }

        document["x"] shouldBe BsonNull()
        document["y"] shouldBe BsonString("hello")
    }

    "Encode class without defaults" {
        val clazz = DataClassWithDefaults("test")
        val document = bson.encodeToBsonDocument(clazz)
            .also { println(it) }

        document.keys shouldContainExactly listOf("s")
    }
})
