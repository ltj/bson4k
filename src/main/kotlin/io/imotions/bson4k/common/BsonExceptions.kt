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

package io.imotions.bson4k.common

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor

internal const val allowStructuredMapKeysHint =
    "Use 'allowStructuredMapKeys = true' in 'Json {}' builder to convert such maps to " +
        "[key1, value1, key2, value2,...] arrays."

sealed class BsonException(message: String) : SerializationException(message)

class BsonEncodingException(message: String) : BsonException(message)

class BsonDecodingException(message: String) : BsonException(message)

fun rootNotDocumentException() = BsonEncodingException(
    "BSON is a document format and serialization must be able to produce a document as the root structure for" +
        "containing other types. Hence primitives and arrays cannot be serialized without an enclosing type."
)

@ExperimentalSerializationApi
fun invalidKeyKindException(keyDescriptor: SerialDescriptor) = BsonEncodingException(
    "Value of type '${keyDescriptor.serialName}' cannot be used as a BSON map key. " +
        "It should have either primitive or enum kind, but its kind is '${keyDescriptor.kind}'.\n" +
        allowStructuredMapKeysHint
)

@ExperimentalSerializationApi
fun missingClassDiscriminatorException(expectedDiscriminator: String, descriptor: SerialDescriptor) =
    BsonDecodingException(
        "The expected class discriminator '$expectedDiscriminator', was not found when decoding the " +
            "polymorphic type '${descriptor.serialName}'."
    )
