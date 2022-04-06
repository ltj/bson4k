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
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import org.bson.Document

@ExperimentalSerializationApi
class BsonPolymorphicDecoderTest : StringSpec({
    "Decode polymorphic type" {
        val doc = Document(CLASS_DISCRIMINATOR, SealedClass.PolyOne::class.qualifiedName)
            .append("value", 42)
            .toBsonDocument()
        println(doc.toJson())

        val deserialized = bson.decodeFromBsonDocument<SealedClass>(doc)
        deserialized shouldBe SealedClass.PolyOne(42)
    }

    "Throw error on unknown class discriminator" {
        val doc = Document("#type", SealedClass.PolyOne::class.qualifiedName)
            .append("value", 42)
            .toBsonDocument()
        println(doc.toJson())

        shouldThrow<SerializationException> { bson.decodeFromBsonDocument<SealedClass>(doc) }
    }

    "Decode wrapped polymorphic type" {
        val doc = Document(
            "value",
            Document(
                CLASS_DISCRIMINATOR,
                SealedClass.PolyTwo::class.qualifiedName
            )
                .append("value", "text")
        ).toBsonDocument()
        println(doc.toJson())

        val deserialized = bson.decodeFromBsonDocument<Wrapper<SealedClass>>(doc)
        deserialized shouldBe Wrapper(SealedClass.PolyTwo("text"))
    }

    "Decode wrapper polymorphic array" {
        val list = listOf(
            SealedClass.PolyOne(42),
            SealedClass.PolyTwo("text")
        )
        val doc = Document(
            "collection",
            listOf(
                Document(CLASS_DISCRIMINATOR, SealedClass.PolyOne::class.qualifiedName)
                    .append("value", 42),
                Document(CLASS_DISCRIMINATOR, SealedClass.PolyTwo::class.qualifiedName)
                    .append("value", "text")
            )
        ).toBsonDocument()
        println(doc.toJson())

        val deserialized = bson.decodeFromBsonDocument<CollectionWrapper<SealedClass>>(doc)
        deserialized shouldBe CollectionWrapper(list)
    }

    "Decode polymorphic object type" {
        val doc = Document(CLASS_DISCRIMINATOR, SealedClass.PolyStatic::class.qualifiedName)
            .toBsonDocument()
        println(doc.toJson())

        val deserialized = bson.decodeFromBsonDocument<SealedClass>(doc)
        deserialized shouldBe SealedClass.PolyStatic
    }
})
