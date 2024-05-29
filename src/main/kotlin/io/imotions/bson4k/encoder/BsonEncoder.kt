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

package io.imotions.bson4k.encoder

import io.imotions.bson4k.BsonConf
import io.imotions.bson4k.BsonKind
import io.imotions.bson4k.common.BsonEncodingException
import io.imotions.bson4k.common.invalidKeyKindException
import io.imotions.bson4k.common.rootNotDocumentException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.SerializersModule
import org.bson.BsonBinary
import org.bson.BsonDocument
import org.bson.BsonDocumentWriter
import org.bson.UuidRepresentation
import org.bson.types.ObjectId
import java.time.Instant
import java.util.*

@ExperimentalSerializationApi
class BsonEncoder(
    private val conf: BsonConf
) : AbstractEncoder() {
    override val serializersModule: SerializersModule
        get() = conf.serializersModule

    private val writer: BsonDocumentWriter = BsonDocumentWriter(BsonDocument())
    private var state = State.ROOT
    private var useMapper: BsonKind? = null

    val document: BsonDocument
        get() {
            writer.flush()
            return writer.document
        }

    override fun shouldEncodeElementDefault(descriptor: SerialDescriptor, index: Int): Boolean =
        conf.encodeDefaults

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        when (descriptor.kind) {
            StructureKind.CLASS -> if (state == State.POLYMORPHIC) {
                state = State.BEGIN
            } else {
                writer.writeStartDocument()
            }
            StructureKind.MAP -> {
                state = if (descriptor.elementDescriptors.first().kind is StructureKind.CLASS) {
                    if (conf.allowStructuredMapKeys) {
                        writer.writeStartArray()
                        State.STRUCTURED_MAP
                    } else {
                        throw invalidKeyKindException(descriptor)
                    }
                } else {
                    writer.writeStartDocument()
                    State.MAP
                }
            }
            StructureKind.OBJECT -> if (state == State.POLYMORPHIC) {
                state = State.BEGIN
            } else {
                writer.writeStartDocument()
            }
            StructureKind.LIST -> {
                if (state == State.ROOT) throw rootNotDocumentException()
                writer.writeStartArray()
            }
            is PolymorphicKind -> {
                writer.writeStartDocument()
                state = State.POLYMORPHIC
            }
            else -> throw BsonEncodingException("Unsupported structure kind: ${descriptor.kind}")
        }
        if (state == State.ROOT) state = State.BEGIN
        return this
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        when (descriptor.kind) {
            StructureKind.LIST -> writer.writeEndArray()
            StructureKind.MAP -> if (state == State.STRUCTURED_MAP) {
                writer.writeEndArray()
            } else {
                writer.writeEndDocument()
            }
            is StructureKind -> if (state != State.POLYMORPHIC) writer.writeEndDocument()
            is PolymorphicKind -> if (state == State.POLYMORPHIC) {
                writer.writeEndDocument()
                state = State.BEGIN
            }
            else -> throw BsonEncodingException("Unsupported structure kind: ${descriptor.kind}")
        }
    }

    override fun encodeElement(descriptor: SerialDescriptor, index: Int): Boolean {
        when {
            descriptor.kind is StructureKind.CLASS -> writer.writeName(descriptor.getElementName(index))
            descriptor.kind is StructureKind.MAP && state != State.STRUCTURED_MAP -> state = when (state) {
                State.MAP_KEY -> State.MAP_VALUE
                else -> State.MAP_KEY
            }
            descriptor.kind is StructureKind.OBJECT -> writer.writeName(descriptor.getElementName(index))
            descriptor.kind is PolymorphicKind && (descriptor.getElementName(index) == "type") ->
                writer.writeName(conf.classDiscriminator)
            descriptor.kind is PrimitiveKind ->
                println(descriptor.serialName)
        }

        useMapper =
            if (descriptor.getElementDescriptor(index).isNullable) {
                conf.bsonTypeMappings[descriptor.getElementDescriptor(index).serialName.removeSuffix("?")]
            } else {
                conf.bsonTypeMappings[descriptor.getElementDescriptor(index).serialName]
            }

        return true
    }

    override fun encodeString(value: String) {
        when (useMapper) {
            BsonKind.OBJECT_ID -> encodeBsonObjectId(value)
            BsonKind.UUID -> encodeUUID(value)
            BsonKind.DATE -> encodeBsonDateTime(value)
            else -> encodeBsonElement(value, writer::writeString)
        }
    }

    override fun encodeInt(value: Int) = encodeBsonElement(value, writer::writeInt32)

    override fun encodeBoolean(value: Boolean) = encodeBsonElement(value, writer::writeBoolean)

    override fun encodeByte(value: Byte) = encodeInt(value.toInt())

    override fun encodeChar(value: Char) = encodeString(value.toString())

    override fun encodeDouble(value: Double) = encodeBsonElement(value, writer::writeDouble)

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) =
        encodeString(enumDescriptor.getElementName(index))

    override fun encodeFloat(value: Float) = encodeDouble(value.toDouble())

    override fun encodeLong(value: Long) {
        when (useMapper) {
            BsonKind.DATE -> encodeBsonDateTime(value)
            else -> encodeBsonElement(value, writer::writeInt64)
        }
    }

    override fun encodeNull() = writer.writeNull()

    override fun encodeShort(value: Short) = encodeInt(value.toInt())

    fun encodeByteArray(value: ByteArray) = encodeBsonElement(BsonBinary(value), writer::writeBinaryData)

    fun encodeBsonDateTime(value: Long) = encodeBsonElement(value, writer::writeDateTime)

    fun encodeBsonDateTime(value: String) {
        val instant = Instant.parse(value)
        encodeBsonDateTime(instant.toEpochMilli())
    }

    fun encodeBsonObjectId(value: String) = encodeBsonElement(ObjectId(value), writer::writeObjectId)

    fun encodeUUID(uuid: UUID, representation: UuidRepresentation = UuidRepresentation.STANDARD) =
        encodeBsonElement(BsonBinary(uuid, representation), writer::writeBinaryData) { uuid.toString() }

    fun encodeUUID(uuid: String, representation: UuidRepresentation = UuidRepresentation.STANDARD) =
        encodeUUID(UUID.fromString(uuid), representation)

    private fun <T : Any> encodeBsonElement(value: T, writeOps: (T) -> Unit, asString: T.() -> String = Any::toString) {
        when (state) {
            State.ROOT -> throw rootNotDocumentException()
            State.MAP_KEY -> writer.writeName(value.asString())
            else -> writeOps(value)
        }
    }

    internal enum class State {
        ROOT,
        BEGIN,
        POLYMORPHIC,
        MAP,
        STRUCTURED_MAP,
        MAP_KEY,
        MAP_VALUE
    }
}
