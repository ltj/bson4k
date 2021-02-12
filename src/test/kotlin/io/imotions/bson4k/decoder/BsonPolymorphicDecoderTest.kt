package io.imotions.bson4k.decoder

import io.imotions.bson4k.common.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.SerializationException
import org.bson.Document

class BsonPolymorphicDecoderTest : StringSpec({
    "Decode polymorphic type" {
        val doc = Document(CLASS_DISCRIMINATOR, SealedTest.PolyOne::class.qualifiedName)
            .append("value", 42)
            .toBsonDocument()
        println(doc.toJson())

        val deserialized = bson.decodeFromBsonDocument<SealedTest>(doc)
        deserialized shouldBe SealedTest.PolyOne(42)
    }

    "Throw error on unknown class discriminator" {
        val doc = Document("#type", SealedTest.PolyOne::class.qualifiedName)
            .append("value", 42)
            .toBsonDocument()
        println(doc.toJson())

        shouldThrow<SerializationException> { bson.decodeFromBsonDocument<SealedTest>(doc) }
    }

    "Decode wrapped polymorphic type" {
        val doc = Document(
            "value", Document(
                CLASS_DISCRIMINATOR,
                SealedTest.PolyTwo::class.qualifiedName
            )
                .append("value", "text")
        ).toBsonDocument()
        println(doc.toJson())

        val deserialized = bson.decodeFromBsonDocument<BasicWrapper<SealedTest>>(doc)
        deserialized shouldBe BasicWrapper(SealedTest.PolyTwo("text"))
    }

    "Decode wrapper polymorphic array" {
        val list = listOf(
            SealedTest.PolyOne(42),
            SealedTest.PolyTwo("text")
        )
        val doc = Document(
            "collection", listOf(
                Document(CLASS_DISCRIMINATOR, SealedTest.PolyOne::class.qualifiedName)
                    .append("value", 42),
                Document(CLASS_DISCRIMINATOR, SealedTest.PolyTwo::class.qualifiedName)
                    .append("value", "text")
            )
        ).toBsonDocument()
        println(doc.toJson())

        val deserialized = bson.decodeFromBsonDocument<CollectionWrapper<SealedTest>>(doc)
        deserialized shouldBe CollectionWrapper(list)
    }
})
