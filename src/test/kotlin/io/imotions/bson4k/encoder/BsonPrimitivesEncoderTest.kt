package io.imotions.bson4k.encoder

import io.imotions.bson4k.common.Wrapper
import io.imotions.bson4k.common.VALUE_KEY
import io.imotions.bson4k.common.bson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import org.bson.*

private fun assertPrimitiveStructure(document: BsonDocument) {
    document.size shouldBeExactly 1
    document.firstKey shouldBe VALUE_KEY
}

@ExperimentalSerializationApi
class BsonPrimitivesEncoderTest : StringSpec({
    "Encode and wrap String primitive" {
        checkAll<String> { s ->
            val document = bson.encodeToBsonDocument(Wrapper(s))

            assertPrimitiveStructure(document)
            document.getString(VALUE_KEY) shouldBe BsonString(s)
        }
    }

    "Encode and wrap Int primitive" {
        checkAll<Int> { i ->
            val document = bson.encodeToBsonDocument(Wrapper(i))

            assertPrimitiveStructure(document)
            document.getInt32(VALUE_KEY) shouldBe BsonInt32(i)
        }
    }

    "Encode and wrap Long primitive" {
        checkAll<Long> { l ->
            val document = bson.encodeToBsonDocument(Wrapper(l))

            assertPrimitiveStructure(document)
            document.getInt64(VALUE_KEY) shouldBe BsonInt64(l)
        }
    }

    "Encode and wrap Boolean primitive" {
        checkAll<Boolean> { b ->
            val document = bson.encodeToBsonDocument(Wrapper(b))

            assertPrimitiveStructure(document)
            document.getBoolean(VALUE_KEY) shouldBe BsonBoolean(b)
        }
    }

    "Encode and wrap Double primitive" {
        checkAll<Double> { d ->
            val document = bson.encodeToBsonDocument(Wrapper(d))

            assertPrimitiveStructure(document)
            document.getDouble(VALUE_KEY) shouldBe BsonDouble(d)
        }
    }

    "Encode and wrap Byte primitive" {
        checkAll<Byte> { b ->
            val document = bson.encodeToBsonDocument(Wrapper(b))

            assertPrimitiveStructure(document)
            document.getInt32(VALUE_KEY) shouldBe BsonInt32(b.toInt())
        }
    }

    "Encode and wrap Char primitive" {
        checkAll<Char> { c ->
            val document = bson.encodeToBsonDocument(Wrapper(c))

            assertPrimitiveStructure(document)
            document.getString(VALUE_KEY) shouldBe BsonString(c.toString())
        }
    }

    "Encode and wrap Float primitive" {
        checkAll<Float> { f ->
            val document = bson.encodeToBsonDocument(Wrapper(f))

            assertPrimitiveStructure(document)
            document.getDouble(VALUE_KEY) shouldBe BsonDouble(f.toDouble())
        }
    }

    "Encode nullable value" {
        val document = bson.encodeToBsonDocument(Wrapper<Int?>(null))

        assertPrimitiveStructure(document)
        document[VALUE_KEY] shouldBe BsonNull()
    }

    "Throw exception when attempting top-level primitives" {
        shouldThrow<SerializationException> {
            bson.encodeToBsonDocument("Test")
        }
    }
})