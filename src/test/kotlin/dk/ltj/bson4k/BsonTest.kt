/*
 *  Copyright 2024 Lars Toft Jacobsen
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package dk.ltj.bson4k

import dk.ltj.bson4k.common.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
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
        bson.configuration.serializersModule shouldBe EmptySerializersModule()
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
            addTypeMapping(InstantLongSerializer, BsonKind.DATE)
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

    "Type mapping should apply to only serializers with matching serial name" {
        val mappingBson = Bson {
            addTypeMapping(UUIDSerializer, BsonKind.UUID)
        }
        val clazz = BsonSingleTypeWithSerializer(UUID.randomUUID(), "some", "things")

        val doc = mappingBson.encodeToBsonDocument(clazz).also { println(it) }

        doc["uuid"]?.bsonType shouldBe BsonType.BINARY
        doc["x"]?.bsonType shouldBe BsonType.STRING

        val deserialized = mappingBson.decodeFromBsonDocument<BsonSingleTypeWithSerializer>(doc)

        deserialized shouldBe clazz
    }

    "Type mapping on a nullable type" {
        val mappingBson = Bson {
            addTypeMapping(InstantStringSerializer, BsonKind.DATE)
        }
        val clazz = StringNullableDateContainer(Instant.now())
        val doc = mappingBson.encodeToBsonDocument(clazz).also { println(it) }

        doc["date"]?.bsonType shouldBe BsonType.DATE_TIME

        val deserialized = mappingBson.decodeFromBsonDocument<StringNullableDateContainer>(doc)

        deserialized.date shouldBe Instant.ofEpochMilli(clazz.date!!.toEpochMilli())
    }

    "Type mapping on a nullable type using nulls" {
        val mappingBson = Bson {
            addTypeMapping(InstantStringSerializer, BsonKind.DATE)
        }
        val clazz = StringNullableDateContainer(null)
        val doc = mappingBson.encodeToBsonDocument(clazz).also { println(it) }

        doc["date"]?.bsonType shouldBe BsonType.NULL

        val deserialized = mappingBson.decodeFromBsonDocument<StringNullableDateContainer>(doc)

        deserialized shouldBe clazz
    }

    "Encode and decode without serialized default values" {
        val clazz = Wrapper2Null<Boolean, Boolean>(y = false)
        val doc = bson.encodeToBsonDocument(clazz)

        doc.keys shouldContainExactly listOf("y")

        val deserialized = bson.decodeFromBsonDocument<Wrapper2Null<Boolean, Boolean>>(doc)

        deserialized shouldBe Wrapper2Null(null, false)
    }

    "Adding an invalid type mapping in the builder should throw" {
        shouldThrow<java.lang.IllegalArgumentException> {
            Bson {
                addTypeMapping(InstantLongSerializer, BsonKind.UUID)
            }
        }
    }
})
