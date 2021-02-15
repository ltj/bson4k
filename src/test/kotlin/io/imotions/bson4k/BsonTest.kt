package io.imotions.bson4k

import io.imotions.bson4k.common.BsonTypesWithSerializers
import io.imotions.bson4k.common.InstantSerializer
import io.imotions.bson4k.common.ObjectIdSerializer
import io.imotions.bson4k.common.UUIDSerializer
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.EmptySerializersModule
import org.bson.BsonType
import org.bson.types.ObjectId
import java.time.Instant
import java.util.*

@ExperimentalSerializationApi
class BsonTest : StringSpec({
    "Builder should return Bson with default configuration" {
        val bson = Bson { }
        bson.configuration.classDiscriminator shouldBe CLASS_DISCRIMINATOR
        bson.configuration.serializersModule shouldBe EmptySerializersModule
    }

    "Builder should fail on invalid chars in class discriminator" {
        shouldThrow<IllegalArgumentException> {
            Bson { classDiscriminator = "\$type" }
        }

        shouldThrow<IllegalArgumentException> {
            Bson { classDiscriminator = "type.." }
        }
    }

    "Type mappings should map primitive types to bson types on encoding and decoding" {
        val mappingBson = Bson {
            addTypeMapping(UUIDSerializer, BsonKind.UUID)
            addTypeMapping(InstantSerializer, BsonKind.DATE)
            addTypeMapping(ObjectIdSerializer, BsonKind.OBJECT_ID)
        }
        val clazz = BsonTypesWithSerializers(UUID.randomUUID(), Instant.now(), ObjectId.get())

        val doc = mappingBson.encodeToBsonDocument(clazz).also { println(it) }

        doc["uuid"]?.bsonType shouldBe BsonType.BINARY
        doc["date"]?.bsonType shouldBe BsonType.DATE_TIME
        doc["objectId"]?.bsonType shouldBe BsonType.OBJECT_ID

        val deserialized = mappingBson.decodeFromBsonDocument<BsonTypesWithSerializers>(doc)

        deserialized shouldBe clazz.copy(date = Instant.ofEpochMilli(clazz.date.toEpochMilli()))
    }

    "Adding an invalid type mapping in the builder should throw" {
        shouldThrow<java.lang.IllegalArgumentException> {
            Bson {
                addTypeMapping(UUIDSerializer, BsonKind.DATE)
            }
        }
    }
})
