package io.imotions.bson4k.decoder

import io.imotions.bson4k.common.bson
import io.imotions.bson4k.encoder.PrimitiveWrapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import org.bson.Document

private val valueKey = "value"

@ExperimentalSerializationApi
class BsonPrimitivesDecoderTest : StringSpec({
    "Decode wrapped String primitives" {
        checkAll<String> { s ->
            val document = Document(valueKey, s).toBsonDocument()
            val wrapper = bson.decodeFromBsonDocument<PrimitiveWrapper<String>>(document)
            wrapper.value shouldBe s
        }
    }

    "Decode wrapped Int primitives" {
        checkAll<Int> { i ->
            val document = Document(valueKey, i).toBsonDocument()
            val wrapper = bson.decodeFromBsonDocument<PrimitiveWrapper<Int>>(document)
            wrapper.value shouldBe i
        }
    }

    "Decode wrapped Long primitives" {
        checkAll<Long> { l ->
            val document = Document(valueKey, l).toBsonDocument()
            val wrapper = bson.decodeFromBsonDocument<PrimitiveWrapper<Long>>(document)
            wrapper.value shouldBe l
        }
    }

    "Decode wrapped Short primitives" {
        checkAll<Short> { s ->
            val document = Document(valueKey, s).toBsonDocument()
            val wrapper = bson.decodeFromBsonDocument<PrimitiveWrapper<Short>>(document)
            wrapper.value shouldBe s
        }
    }

    "Decode wrapped Byte primitives" {
        checkAll<Byte> { b ->
            val document = Document(valueKey, b).toBsonDocument()
            val wrapper = bson.decodeFromBsonDocument<PrimitiveWrapper<Byte>>(document)
            wrapper.value shouldBe b
        }
    }

    "Decode wrapped Char primitives" {
        checkAll<Char> { c ->
            val document = Document(valueKey, c).toBsonDocument()
            val wrapper = bson.decodeFromBsonDocument<PrimitiveWrapper<Char>>(document)
            wrapper.value shouldBe c
        }
    }

    "Decode wrapped Float primitives" {
        checkAll<Float> { f ->
            val document = Document(valueKey, f).toBsonDocument()
            val wrapper = bson.decodeFromBsonDocument<PrimitiveWrapper<Float>>(document)
            wrapper.value shouldBe f
        }
    }

    "Decode wrapped Double primitives" {
        checkAll<Double> { d ->
            val document = Document(valueKey, d).toBsonDocument()
            val wrapper = bson.decodeFromBsonDocument<PrimitiveWrapper<Double>>(document)
            wrapper.value shouldBe d
        }
    }

    "Decode wrapped Boolean primitives" {
        checkAll<Boolean> { b ->
            val document = Document(valueKey, b).toBsonDocument()
            val wrapper = bson.decodeFromBsonDocument<PrimitiveWrapper<Boolean>>(document)
            wrapper.value shouldBe b
        }
    }

    "Decode nullable value" {
        val document = Document(valueKey, null).toBsonDocument()
        val wrapper = bson.decodeFromBsonDocument<PrimitiveWrapper<Int?>>(document)
        wrapper.value shouldBe null
    }

    "Throw exception if attempting to decode directly to primitive type" {
        shouldThrow<SerializationException> {
            bson.decodeFromBsonDocument<String>(Document().toBsonDocument())
        }
    }
})
