package io.imotions.bson4k.decoder

import io.imotions.bson4k.Bson
import io.imotions.bson4k.BsonKind
import io.imotions.bson4k.common.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.MapSerializer
import org.bson.BsonBinary
import org.bson.Document
import org.bson.UuidRepresentation
import java.util.*

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

    "Decode to map with non-primitive keys" {
        val doc = Document(UUID.randomUUID().toString(), Document("uuid", UUID.randomUUID().toString()))
            .append(UUID.randomUUID().toString(), Document("uuid", UUID.randomUUID().toString()))

        bson.decodeFromBsonDocument(
            MapSerializer(
                UUIDSerializer,
                StringUUIDContainer.serializer()
            ),
            doc.toBsonDocument()
        )
    }

    "Decode to map with non-primitive keys and type mapping" {
        val mappingBson = Bson {
            addTypeMapping(UUIDSerializer, BsonKind.UUID)
        }
        val doc = Document(
            UUID.randomUUID().toString(),
            Document("uuid", BsonBinary(UUID.randomUUID(), UuidRepresentation.STANDARD))
        )
            .append(
                UUID.randomUUID().toString(),
                Document(
                    "uuid", BsonBinary(UUID.randomUUID(), UuidRepresentation.STANDARD)
                )
            )
            .also { println(it) }

        mappingBson.decodeFromBsonDocument(
            MapSerializer(
                UUIDSerializer,
                StringUUIDContainer.serializer()
            ),
            doc.toBsonDocument()
        )
    }
})
