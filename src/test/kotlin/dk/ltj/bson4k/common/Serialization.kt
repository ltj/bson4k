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

package dk.ltj.bson4k.common

import dk.ltj.bson4k.Bson
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import org.bson.types.ObjectId
import java.time.Instant
import java.util.*

@ExperimentalSerializationApi
val bson = Bson { }
val json = Json { classDiscriminator = CLASS_DISCRIMINATOR }

const val CLASS_DISCRIMINATOR = "__type"

object UUIDSerializer : KSerializer<UUID> {
    override fun deserialize(decoder: Decoder): UUID =
        UUID.fromString(decoder.decodeString())

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("dk.ltj.bson4k.uuid", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UUID) =
        encoder.encodeString(value.toString())
}

object InstantLongSerializer : KSerializer<Instant> {
    override fun deserialize(decoder: Decoder): Instant =
        Instant.ofEpochMilli(decoder.decodeLong())

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("dk.ltj.bson4k.instant", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Instant) =
        encoder.encodeLong(value.toEpochMilli())
}

object InstantStringSerializer : KSerializer<Instant> {
    override fun deserialize(decoder: Decoder): Instant =
        Instant.parse(decoder.decodeString())

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("dk.ltj.bson4k.instant", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) =
        encoder.encodeString(value.toString())
}

object ObjectIdSerializer : KSerializer<ObjectId> {
    override fun deserialize(decoder: Decoder): ObjectId =
        ObjectId(decoder.decodeString())

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("dk.ltj.bson4k.objectid", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ObjectId) =
        encoder.encodeString(value.toHexString())
}

@ExperimentalSerializationApi
object CustomNullableSerializer : KSerializer<String?> {
    override fun deserialize(decoder: Decoder): String? {
        if (decoder.decodeNotNullMark()) {
            return decoder.decodeString()
        } else {
            // Return null vs decoder.decodeNull() should not matter
            return null
        }
    }

    override val descriptor: SerialDescriptor = String.serializer().nullable.descriptor

    override fun serialize(encoder: Encoder, value: String?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeString(value)
        }
    }
}
