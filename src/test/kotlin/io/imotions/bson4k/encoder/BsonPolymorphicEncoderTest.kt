package io.imotions.bson4k.encoder

import io.imotions.bson4k.CLASS_DISCRIMINATOR
import io.imotions.bson4k.common.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi

@ExperimentalSerializationApi
class BsonPolymorphicEncoderTest : StringSpec({
    "Encode document using object polymorphism" {
        val polyClass = SealedClass.PolyOne(123)
        val document = bson.encodeToBsonDocument(SealedClass.serializer(), polyClass)
            .also { println(it.toJson()) }

        val serialized = json.decodeFromString(SealedClass.serializer(), document.toJson())
        document.firstKey shouldBe CLASS_DISCRIMINATOR
        serialized shouldBe polyClass
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
})
