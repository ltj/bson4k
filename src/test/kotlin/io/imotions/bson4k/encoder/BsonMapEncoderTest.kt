package io.imotions.bson4k.encoder

import io.imotions.bson4k.common.MapWrapper
import io.imotions.bson4k.common.Wrapper
import io.imotions.bson4k.common.Wrapper2
import io.imotions.bson4k.common.bson
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.property.checkAll
import kotlinx.serialization.ExperimentalSerializationApi
import org.bson.BsonInt32

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
})
