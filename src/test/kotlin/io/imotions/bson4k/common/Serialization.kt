package io.imotions.bson4k.common

import io.imotions.bson4k.Bson
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
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
        PrimitiveSerialDescriptor("io.imotions.bson4k.uuid", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UUID) =
        encoder.encodeString(value.toString())
}

object InstantSerializer : KSerializer<Instant> {
    override fun deserialize(decoder: Decoder): Instant =
        Instant.ofEpochMilli(decoder.decodeLong())

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("io.imotions.bson4k.instant", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Instant) =
        encoder.encodeLong(value.toEpochMilli())
}

object ObjectIdSerializer : KSerializer<ObjectId> {
    override fun deserialize(decoder: Decoder): ObjectId =
        ObjectId(decoder.decodeString())

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("io.imotions.bson4k.objectid", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ObjectId) =
        encoder.encodeString(value.toHexString())
}
