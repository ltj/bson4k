/*
 * Copyright 2021 iMotions A/S
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.imotions.bson4k.encoder

import io.imotions.bson4k.Bson
import io.imotions.bson4k.BsonKind
import io.imotions.bson4k.common.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.localDateTime
import io.kotest.property.checkAll
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import org.bson.*
import org.bson.types.ObjectId
import java.time.ZoneOffset
import java.util.*

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

    "Map UUID String to BsonBinary" {
        val mappingBson = Bson {
            addTypeMapping(UUIDSerializer, BsonKind.UUID)
        }

        val uuidGen = arbitrary { UUID.randomUUID() }
        checkAll(uuidGen) { uuid ->
            val document = mappingBson.encodeToBsonDocument(StringUUIDContainer(uuid))
            document["uuid"]?.bsonType shouldBe BsonType.BINARY
        }
    }

    "Map Date Long (epoch ms) to BsonDateTime" {
        val mappingBson = Bson {
            addTypeMapping(InstantLongSerializer, BsonKind.DATE)
        }

        checkAll(Arb.localDateTime(1980, 2030)) { i ->
            val doc = mappingBson.encodeToBsonDocument(LongDateContainer(i.toInstant(ZoneOffset.UTC)))
            doc["date"]?.bsonType shouldBe BsonType.DATE_TIME
        }
    }

    "Map Date String (ISO) to BsonDateTime" {
        val mappingBson = Bson {
            addTypeMapping(InstantStringSerializer, BsonKind.DATE)
        }

        checkAll(Arb.localDateTime(1980, 2030)) { i ->
            val doc = mappingBson.encodeToBsonDocument(StringDateContainer(i.toInstant(ZoneOffset.UTC)))
            doc["date"]?.bsonType shouldBe BsonType.DATE_TIME
        }
    }

    "Map ObjectId string to ObjectId" {
        val mappingBson = Bson {
            addTypeMapping(ObjectIdSerializer, BsonKind.OBJECT_ID)
        }

        val doc = mappingBson.encodeToBsonDocument(StringObjectIdContainer(ObjectId.get()))
        doc["objectId"]?.bsonType shouldBe BsonType.OBJECT_ID
    }

    "Throw exception when attempting top-level primitives" {
        shouldThrow<SerializationException> {
            bson.encodeToBsonDocument("Test")
        }
    }
})
