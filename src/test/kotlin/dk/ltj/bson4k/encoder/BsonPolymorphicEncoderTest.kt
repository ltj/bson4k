/*
 *  Copyright 2024 Lars Toft Jacobsen
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package dk.ltj.bson4k.encoder

import dk.ltj.bson4k.CLASS_DISCRIMINATOR
import dk.ltj.bson4k.common.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi

@ExperimentalSerializationApi
class BsonPolymorphicEncoderTest : StringSpec({
    "Encode document using object polymorphism" {
        val polyClass = SealedClass.PolyOne(123)
        val document = bson.encodeToBsonDocument(SealedClass.serializer(), polyClass)
            .also { println(it.toJson()) }

        val deserialized = json.decodeFromString(SealedClass.serializer(), document.toJson())
        document.firstKey shouldBe CLASS_DISCRIMINATOR
        deserialized shouldBe polyClass
    }

    "Encode wrapped polymorphic type" {
        val polyClass = SealedClass.PolyTwo("text")
        val wrapper = Wrapper<SealedClass>(polyClass)
        val document = bson.encodeToBsonDocument(wrapper)
            .also { println(it.toJson()) }

        val deserialized = json.decodeFromString(Wrapper.serializer(SealedClass.serializer()), document.toJson())
        deserialized shouldBe wrapper
    }

    "Encode wrapped polymorphic array" {
        val collection = listOf(
            SealedClass.PolyOne(123),
            SealedClass.PolyTwo("text"),
            SealedClass.PolyList(
                listOf(
                    SealedClass.PolyOne(Int.MIN_VALUE),
                    SealedClass.PolyOne(Int.MAX_VALUE)
                )
            )
        )
        val wrapper = CollectionWrapper(collection)
        val document = bson.encodeToBsonDocument(wrapper)
            .also { println(it.toJson()) }

        val deserialized =
            json.decodeFromString(CollectionWrapper.serializer(SealedClass.serializer()), document.toJson())
        deserialized shouldBe wrapper
    }

    "Encode sealed object type" {
        val obj = SealedClass.PolyStatic
        val document = bson.encodeToBsonDocument(SealedClass.serializer(), obj).also { println(it.toJson()) }

        document.firstKey shouldBe CLASS_DISCRIMINATOR
        document.keys.size shouldBe 1
    }
})
