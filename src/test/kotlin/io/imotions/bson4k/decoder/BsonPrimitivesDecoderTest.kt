package io.imotions.bson4k.decoder

import io.imotions.bson4k.common.VALUE_KEY
import io.imotions.bson4k.common.Wrapper
import io.imotions.bson4k.common.bson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import org.bson.Document

@ExperimentalSerializationApi
class BsonPrimitivesDecoderTest : StringSpec({
    "Decode wrapped String primitives" {
        checkAll<String> { s ->
            val document = Document(VALUE_KEY, s).toBsonDocument()
            val wrapper = bson.decodeFromBsonDocument<Wrapper<String>>(document)
            wrapper.value shouldBe s
        }
    }

    "Decode wrapped Int primitives" {
        checkAll<Int> { i ->
            val document = Document(VALUE_KEY, i).toBsonDocument()
            val wrapper = bson.decodeFromBsonDocument<Wrapper<Int>>(document)
            wrapper.value shouldBe i
        }
    }

    "Decode wrapped Long primitives" {
        checkAll<Long> { l ->
            val document = Document(VALUE_KEY, l).toBsonDocument()
            val wrapper = bson.decodeFromBsonDocument<Wrapper<Long>>(document)
            wrapper.value shouldBe l
        }
    }

    "Decode wrapped Short primitives" {
        checkAll<Short> { s ->
            val document = Document(VALUE_KEY, s).toBsonDocument()
            val wrapper = bson.decodeFromBsonDocument<Wrapper<Short>>(document)
            wrapper.value shouldBe s
        }
    }

    "Decode wrapped Byte primitives" {
        checkAll<Byte> { b ->
            val document = Document(VALUE_KEY, b).toBsonDocument()
            val wrapper = bson.decodeFromBsonDocument<Wrapper<Byte>>(document)
            wrapper.value shouldBe b
        }
    }

    "Decode wrapped Char primitives" {
        checkAll<Char> { c ->
            val document = Document(VALUE_KEY, c).toBsonDocument()
            val wrapper = bson.decodeFromBsonDocument<Wrapper<Char>>(document)
            wrapper.value shouldBe c
        }
    }

    "Decode wrapped Float primitives" {
        checkAll<Float> { f ->
            val document = Document(VALUE_KEY, f).toBsonDocument()
            val wrapper = bson.decodeFromBsonDocument<Wrapper<Float>>(document)
            wrapper.value shouldBe f
        }
    }

    "Decode wrapped Double primitives" {
        checkAll<Double> { d ->
            val document = Document(VALUE_KEY, d).toBsonDocument()
            val wrapper = bson.decodeFromBsonDocument<Wrapper<Double>>(document)
            wrapper.value shouldBe d
        }
    }

    "Decode wrapped Boolean primitives" {
        checkAll<Boolean> { b ->
            val document = Document(VALUE_KEY, b).toBsonDocument()
            val wrapper = bson.decodeFromBsonDocument<Wrapper<Boolean>>(document)
            wrapper.value shouldBe b
        }
    }

    "Decode nullable value" {
        val document = Document(VALUE_KEY, null).toBsonDocument()
        val wrapper = bson.decodeFromBsonDocument<Wrapper<Int?>>(document)
        wrapper.value shouldBe null
    }

    "Throw exception if attempting to decode directly to primitive type" {
        shouldThrow<SerializationException> {
            bson.decodeFromBsonDocument<String>(Document().toBsonDocument())
        }
    }
})
