package io.imotions.bson4k.decoder

import io.imotions.bson4k.common.MapWrapper
import io.imotions.bson4k.common.Wrapper
import io.imotions.bson4k.common.bson
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
            10 to Wrapper(42.5F),
            20 to Wrapper(123.45F)
        )
        val document = Document(
            "map",
            Document()
                .append("10", Document("value", 42.5F))
                .append("20", Document("value", 123.45F))
        ).toBsonDocument()
        println(document.toJson())

        val deserialized = bson.decodeFromBsonDocument<MapWrapper<Int, Wrapper<Float>>>(document)
        deserialized shouldBe MapWrapper(map)
    }
})
