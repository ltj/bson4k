package io.imotions.bson4k.decoder

import io.imotions.bson4k.common.bson
import io.imotions.bson4k.encoder.TestMapNested
import io.imotions.bson4k.encoder.TestMapWrapper
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi
import org.bson.Document

@ExperimentalSerializationApi
class BsonMapDecoderTest : StringSpec({
    "Decode primitives map" {
        val map = mapOf(
            "a" to true,
            "b" to false
        )
        val document = Document(map).toBsonDocument()
        println(document.toJson())

        val deserialized = bson.decodeFromBsonDocument<Map<String, Boolean>>(document)
        deserialized shouldBe map
    }

    "Decode primitives map with Int keys" {
        val map = mapOf(
            20 to true,
            30 to false
        )
        val document = Document(map.mapKeys { it.key.toString() }).toBsonDocument()
        println(document.toJson())

        val deserialized = bson.decodeFromBsonDocument<Map<Int, Boolean>>(document)
        deserialized shouldBe map
    }

    "Decode to map with composite values" {
        val map = mapOf(
            10 to TestMapNested(42.5F),
            20 to TestMapNested(123.45F)
        )
        val document = Document(
            "map", Document()
                .append("10", Document("z", 42.5F))
                .append("20", Document("z", 123.45F))
        ).toBsonDocument()
        println(document.toJson())

        val deserialized = bson.decodeFromBsonDocument<TestMapWrapper<Int, TestMapNested>>(document)
        deserialized shouldBe TestMapWrapper(map)
    }
})