package io.imotions.bson4k.encoder

import io.imotions.bson4k.Bson
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.property.checkAll
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import org.bson.BsonInt32

@ExperimentalSerializationApi
private val bson = Bson()

@Serializable
data class TestMapNested(val z: Float)

@Serializable
data class TestMapValue(val x: String, val y: TestMapNested)

@Serializable
data class TestMapWrapper<K, V>(val map: Map<K, V>)

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
            10 to TestMapValue("abc", TestMapNested(42.5F)),
            20 to TestMapValue("cde", TestMapNested(123.45F))
        )

        val document = bson.encodeToBsonDocument(map)
            .also { println(it.toJson()) }
    }

    "Encode nested map" {
        val map = mapOf(
            10 to TestMapValue("abc", TestMapNested(42.5F)),
            20 to TestMapValue("cde", TestMapNested(123.45F))
        )
        val wrapper = TestMapWrapper(map)
        val document = bson.encodeToBsonDocument(wrapper)
            .also { println(it.toJson()) }
    }
})
