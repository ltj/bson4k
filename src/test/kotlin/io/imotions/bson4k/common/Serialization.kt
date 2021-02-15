package io.imotions.bson4k.common

import io.imotions.bson4k.Bson
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@ExperimentalSerializationApi
val bson = Bson { }
val json = Json { classDiscriminator = CLASS_DISCRIMINATOR }

const val CLASS_DISCRIMINATOR = "__type"
