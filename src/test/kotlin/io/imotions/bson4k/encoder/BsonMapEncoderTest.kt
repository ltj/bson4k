package io.imotions.bson4k.encoder

import io.imotions.bson4k.Bson
import io.imotions.bson4k.BsonKind
import io.imotions.bson4k.common.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.property.checkAll
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.MapSerializer
import org.bson.BsonInt32
import java.util.*

@ExperimentalSerializationApi
class BsonMapEncoderTest : StringSpec({
    "Encode map as document" {
        checkAll<Map<String, Int>> { m ->
            val document = bson.encodeToBsonDocument(m)
            document.keys shouldContainAll m.keys
            document.values shouldContainAll m.values.map { BsonInt32(it) }
        }
    }

    "Encode map with composite values" {
        val map = mapOf(
            10 to Wrapper2("abc", Wrapper(42.5F)),
            20 to Wrapper2("cde", Wrapper(123.45F))
        )

        bson.encodeToBsonDocument(map)
            .also { println(it.toJson()) }
    }

    "Encode nested map" {
        val map = mapOf(
            10 to Wrapper2("abc", Wrapper(42.5F)),
            20 to Wrapper2("cde", Wrapper(123.45F))
        )
        val wrapper = MapWrapper(map)
        bson.encodeToBsonDocument(wrapper)
            .also { println(it.toJson()) }
    }

    "Encode map with non-primitive keys" {
        val map = mapOf(
            UUID.randomUUID() to StringUUIDContainer(UUID.randomUUID()),
            UUID.randomUUID() to StringUUIDContainer(UUID.randomUUID())
        )
        val doc = bson.encodeToBsonDocument(MapSerializer(UUIDSerializer, StringUUIDContainer.serializer()), map)
            .also { println(it) }

        doc.keys shouldContainAll map.keys.map { it.toString() }
    }

    "Encode map with non-primitive keys using type mapping" {
        val mappingBson = Bson {
            addTypeMapping(UUIDSerializer, BsonKind.UUID)
        }
        val map = mapOf(
            UUID.randomUUID() to StringUUIDContainer(UUID.randomUUID()),
            UUID.randomUUID() to StringUUIDContainer(UUID.randomUUID())
        )
        val doc = mappingBson.encodeToBsonDocument(MapSerializer(UUIDSerializer, StringUUIDContainer.serializer()), map)
            .also { println(it) }

        doc.keys shouldContainAll map.keys.map { it.toString() }
    }
})
