package io.imotions.bson4k

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

const val CLASS_DISCRIMINATOR = "__type"

typealias SerialName = String

@ExperimentalSerializationApi
data class BsonConf(
    val serializersModule: SerializersModule = EmptySerializersModule,
    val classDiscriminator: String = CLASS_DISCRIMINATOR,
    val bsonTypeMappings: Map<SerialName, BsonTypeMapping> = emptyMap()
)
