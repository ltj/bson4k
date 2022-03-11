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

package io.imotions.bson4k

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

const val CLASS_DISCRIMINATOR = "__type"

typealias SerialName = String

@ExperimentalSerializationApi
data class BsonConf internal constructor(
    val serializersModule: SerializersModule = EmptySerializersModule,
    val classDiscriminator: String = CLASS_DISCRIMINATOR,
    val bsonTypeMappings: Map<SerialName, BsonKind> = emptyMap(),
    val allowStructuredMapKeys: Boolean = false,
    val implicitIntegerConversion: Boolean = true
)
