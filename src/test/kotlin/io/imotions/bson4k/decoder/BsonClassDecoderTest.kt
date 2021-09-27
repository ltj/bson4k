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

package io.imotions.bson4k.decoder

import io.imotions.bson4k.common.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi
import org.bson.Document

@ExperimentalSerializationApi
class BsonClassDecoderTest : StringSpec({
    "Decode simple class" {
        val document = Document("x", "hello").append("y", 34.5F).toBsonDocument()
        val testClass = bson.decodeFromBsonDocument<Wrapper2<String, Float>>(document)

        testClass shouldBe Wrapper2("hello", 34.5F)
    }

    "Decode simple class with fields reversed" {
        val document = Document("y", 34.5F).append("x", "hello").toBsonDocument()
        val testClass = bson.decodeFromBsonDocument<Wrapper2<String, Float>>(document)

        testClass shouldBe Wrapper2("hello", 34.5F)
    }

    "Decode nested class" {
        val document = Document("value", Document("x", "world").append("y", 10.45F))
        val nestingTestClass = bson.decodeFromBsonDocument<Wrapper<Wrapper2<String, Float>>>(document.toBsonDocument())

        nestingTestClass shouldBe Wrapper(Wrapper2("world", 10.45F))
    }

    "Decode enum field" {
        val doc = Document("x", "enum fields").append("y", "FOURTH")
        val wrapper = bson.decodeFromBsonDocument<Wrapper2<String, EnumClass>>(doc.toBsonDocument())
        wrapper shouldBe Wrapper2("enum fields", EnumClass.FOURTH)
    }

    "Decode class with nullables" {
        val doc = Document("x", null).append("y", 42)
        val wrapper = bson.decodeFromBsonDocument<Wrapper2<String?, Int?>>(doc.toBsonDocument())
        wrapper shouldBe Wrapper2(null, 42)
    }

    "Decode class with nullable defaults present" {
        val doc = Document("x", null).append("y", null)
        val wrapper = bson.decodeFromBsonDocument<Wrapper2Null<String, String>>(doc.toBsonDocument())
        wrapper shouldBe Wrapper2Null(y = null)
    }

    "Decode class with custom nullable serializer" {
        val doc = Document("a", null).append("b", null)
        val deserialized = bson.decodeFromBsonDocument<CustomNullables>(doc.toBsonDocument())
        deserialized shouldBe CustomNullables(null)
    }
})
