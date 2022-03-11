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

import io.imotions.bson4k.decoder.BsonDecoder
import io.imotions.bson4k.encoder.BsonEncoder
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.modules.SerializersModule
import org.bson.BsonDocument
import org.bson.BsonDocumentReader

@ExperimentalSerializationApi
class Bson(val configuration: BsonConf) : SerialFormat, StringFormat {

    override val serializersModule: SerializersModule
        get() = configuration.serializersModule

    fun <T> encodeToBsonDocument(serializer: SerializationStrategy<T>, value: T): BsonDocument {
        val encoder = BsonEncoder(configuration)
        encoder.encodeSerializableValue(serializer, value)
        return encoder.document
    }

    inline fun <reified T> encodeToBsonDocument(value: T): BsonDocument = encodeToBsonDocument(serializer(), value)

    fun <T> decodeFromBsonDocument(deserializer: DeserializationStrategy<T>, document: BsonDocument): T {
        val reader = BsonDocumentReader(document)
        val decoder = BsonDecoder(reader, configuration)
        return decoder.decodeSerializableValue(deserializer)
    }

    inline fun <reified T> decodeFromBsonDocument(document: BsonDocument): T =
        decodeFromBsonDocument(serializer(), document)

    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
        return decodeFromBsonDocument(deserializer, BsonDocument.parse(string))
    }

    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String {
        return encodeToBsonDocument(serializer, value).toJson()
    }
}

@ExperimentalSerializationApi
fun Bson(builderAction: BsonBuilder.() -> Unit): Bson {
    val builder = BsonBuilder(BsonConf())
    builder.builderAction()
    return Bson(builder.build())
}

@ExperimentalSerializationApi
class BsonBuilder internal constructor(conf: BsonConf) {
    var classDiscriminator = conf.classDiscriminator
    var serializersModule = conf.serializersModule
    var allowStructuredMapKeys = conf.allowStructuredMapKeys
    var implicitIntegerConversion = conf.implicitIntegerConversion
    var encodeDefaults = conf.encodeDefaults
    internal val bsonTypeMappings = conf.bsonTypeMappings.toMutableMap()

    fun addTypeMapping(serializer: KSerializer<*>, bsonKind: BsonKind) {
        require(serializer.descriptor.kind in bsonKind.supportedKinds) {
            "Mapping to and from ${serializer.descriptor.kind} is not supported by $bsonKind"
        }
        bsonTypeMappings[serializer.descriptor.serialName] = bsonKind
    }

    fun build(): BsonConf {
        require(!classDiscriminator.contains("""[$.]""".toRegex())) {
            "Class discriminator cannot contain illegal BSON field characters: [$.]"
        }
        return BsonConf(
            classDiscriminator = classDiscriminator,
            serializersModule = serializersModule,
            bsonTypeMappings = bsonTypeMappings,
            allowStructuredMapKeys = allowStructuredMapKeys,
            implicitIntegerConversion = implicitIntegerConversion,
            encodeDefaults = encodeDefaults
        )
    }
}

enum class BsonKind(val supportedKinds: List<PrimitiveKind>) {
    DATE(listOf(PrimitiveKind.LONG, PrimitiveKind.STRING)),
    OBJECT_ID(listOf(PrimitiveKind.STRING)),
    UUID(listOf(PrimitiveKind.STRING))
}
