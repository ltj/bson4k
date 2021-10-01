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
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.MapSerializer
import org.bson.BsonInt32
import org.bson.BsonType
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

    "Encode map with composite keys" {
        val structuredBson = Bson {
            allowStructuredMapKeys = true
        }
        val map = mapOf(
            Wrapper2("foo", "bar") to 0L,
            Wrapper2("hello", "world") to 10_000_000L
        )
        val wrapper = Wrapper(map)
        val doc = structuredBson.encodeToBsonDocument(wrapper)
            .also { println(it) }

        doc.getArray("value").forEachIndexed { index, bsonValue ->
            bsonValue.bsonType shouldBe if (index % 2 == 0) {
                BsonType.DOCUMENT
            } else {
                BsonType.INT64
            }
        }
    }

    "Encode map with composite key throws BsonEncodingException without allowedStructuredMapKeys" {
        val map = mapOf(
            Wrapper2("foo", "bar") to 0L,
            Wrapper2("hello", "world") to 10_000_000L
        )
        val wrapper = Wrapper(map)
        shouldThrow<BsonEncodingException> { bson.encodeToBsonDocument(wrapper) }
    }
})
