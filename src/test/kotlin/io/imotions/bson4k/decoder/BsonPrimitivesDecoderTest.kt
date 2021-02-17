package io.imotions.bson4k.decoder

import io.imotions.bson4k.Bson
import io.imotions.bson4k.BsonKind
import io.imotions.bson4k.common.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.localDateTime
import io.kotest.property.checkAll
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import org.bson.BsonBinary
import org.bson.BsonDateTime
import org.bson.Document
import org.bson.UuidRepresentation
import org.bson.types.ObjectId
import java.time.ZoneOffset
import java.util.*

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

    "Map BsonBinary to UUID string" {
        val mappingBson = Bson {
            addTypeMapping(UUIDSerializer, BsonKind.UUID)
        }

        val uuidGen = arbitrary { UUID.randomUUID() }
        checkAll(uuidGen) { uuid ->
            val doc = Document("uuid", BsonBinary(uuid, UuidRepresentation.STANDARD)).toBsonDocument()
            val container = mappingBson.decodeFromBsonDocument<StringUUIDContainer>(doc)
            container.uuid shouldBe uuid
        }
    }

    "Map BsonDate to Instant long representation" {
        val mappingBson = Bson {
            addTypeMapping(InstantLongSerializer, BsonKind.DATE)
        }

        checkAll(Arb.localDateTime(1980, 2030)) { d ->
            val doc = Document("date", BsonDateTime(d.toInstant(ZoneOffset.UTC).toEpochMilli())).toBsonDocument()
            val container = mappingBson.decodeFromBsonDocument<LongDateContainer>(doc)
            container.date shouldBe d.toInstant(ZoneOffset.UTC)
        }
    }

    "Map BsonDate to Instant string representation" {
        val mappingBson = Bson {
            addTypeMapping(InstantStringSerializer, BsonKind.DATE)
        }

        checkAll(Arb.localDateTime(1980, 2030)) { d ->
            val doc = Document("date", BsonDateTime(d.toInstant(ZoneOffset.UTC).toEpochMilli())).toBsonDocument()
            val container = mappingBson.decodeFromBsonDocument<StringDateContainer>(doc)
            container.date shouldBe d.toInstant(ZoneOffset.UTC)
        }
    }

    "Map ObjectId to ObjectId string representation" {
        val mappingBson = Bson {
            addTypeMapping(ObjectIdSerializer, BsonKind.OBJECT_ID)
        }
        val oid = ObjectId.get()
        val doc = Document("objectId", oid).toBsonDocument()
        val container = mappingBson.decodeFromBsonDocument<StringObjectIdContainer>(doc)
        container.objectId shouldBe oid
    }

    "Throw exception if attempting to decode directly to primitive type" {
        shouldThrow<SerializationException> {
            bson.decodeFromBsonDocument<String>(Document().toBsonDocument())
        }
    }
})
