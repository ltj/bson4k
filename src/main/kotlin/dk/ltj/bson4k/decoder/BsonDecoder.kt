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

package dk.ltj.bson4k.decoder

import dk.ltj.bson4k.BsonConf
import dk.ltj.bson4k.BsonKind
import dk.ltj.bson4k.common.BsonDecodingException
import dk.ltj.bson4k.common.missingClassDiscriminatorException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.modules.SerializersModule
import org.bson.AbstractBsonReader
import org.bson.AbstractBsonReader.State.*
import org.bson.BsonBinary
import org.bson.BsonType
import org.bson.types.ObjectId
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayDeque

@ExperimentalSerializationApi
class BsonDecoder(
    private val reader: AbstractBsonReader,
    private val conf: BsonConf
) : AbstractDecoder() {
    override val serializersModule: SerializersModule
        get() = conf.serializersModule

    private var stateStack = ArrayDeque<Pair<DecoderState, Int>>()
    private var state = DecoderState.DOCUMENT
    private var currentIndex = 0
    private var useMapper: BsonKind? = null

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        val idx = when (state) {
            DecoderState.DOCUMENT -> {
                if (reader.state == TYPE) {
                    reader.readBsonType()
                }
                if (reader.state == NAME) {
                    descriptor.getElementIndex(reader.readName())
                } else {
                    DECODE_DONE
                }
            }

            DecoderState.LIST -> {
                val type = reader.readBsonType()
                if (type == BsonType.END_OF_DOCUMENT) DECODE_DONE else currentIndex++
            }

            DecoderState.MAP_KEY -> {
                val type = reader.readBsonType()
                if (type == BsonType.END_OF_DOCUMENT) DECODE_DONE else 0
            }

            DecoderState.MAP_VALUE -> 1
            DecoderState.POLYMORPHIC -> {
                when (currentIndex) {
                    0 -> {
                        if (reader.state == TYPE) {
                            reader.readBsonType()
                        }
                        if (reader.readName() == conf.classDiscriminator) {
                            currentIndex++
                        } else {
                            throw missingClassDiscriminatorException(conf.classDiscriminator, descriptor)
                        }
                    }

                    1 -> currentIndex++
                    else -> DECODE_DONE
                }
            }
        }

        if (idx >= 0) {
            useMapper = if (state == DecoderState.MAP_KEY) {
                null // Don't return a mapped type as key
            } else {
                if (descriptor.getElementDescriptor(idx).isNullable) {
                    conf.bsonTypeMappings[descriptor.getElementDescriptor(idx).serialName.removeSuffix("?")]
                } else {
                    conf.bsonTypeMappings[descriptor.getElementDescriptor(idx).serialName]
                }
            }
        }

        return idx
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        preserveState()
        when (descriptor.kind) {
            StructureKind.LIST -> {
                beginListStructure()
            }

            StructureKind.MAP -> {
                if (descriptor.elementDescriptors.first().kind == StructureKind.CLASS && conf.allowStructuredMapKeys) {
                    beginListStructure()
                } else {
                    state = DecoderState.MAP_KEY
                    reader.readStartDocument()
                }
            }

            is PolymorphicKind -> {
                state = DecoderState.POLYMORPHIC
                currentIndex = 0
                reader.readStartDocument()
            }

            is StructureKind -> {
                if (state != DecoderState.POLYMORPHIC) {
                    reader.readStartDocument()
                }
                state = DecoderState.DOCUMENT
            }

            else -> throw BsonDecodingException("Illegal begin structure kind: ${descriptor.kind}")
        }
        return super.beginStructure(descriptor)
    }

    private fun beginListStructure() {
        state = DecoderState.LIST
        currentIndex = 0
        reader.readStartArray()
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        restoreState()
        when (descriptor.kind) {
            StructureKind.LIST -> {
                reader.readEndArray()
            }

            StructureKind.MAP -> {
                if (reader.state == END_OF_ARRAY) {
                    reader.readEndArray()
                } else {
                    reader.readEndDocument()
                }
            }

            is StructureKind -> {
                reader.readEndDocument()
            }

            else -> {} // noop
        }
        super.endStructure(descriptor)
    }

    override fun decodeNotNullMark(): Boolean {
        return if (reader.currentBsonType == BsonType.NULL) {
            reader.readNull()
            false
        } else {
            true
        }
    }

    override fun decodeBoolean(): Boolean = decodeBsonElement(reader::readBoolean, String::toBoolean)

    override fun decodeByte(): Byte = decodeInt().toByte()

    override fun decodeChar(): Char = decodeString().first()

    override fun decodeDouble(): Double = decodeBsonElement(reader::readDouble, String::toDouble)

    override fun decodeFloat(): Float = decodeDouble().toFloat()

    override fun decodeInt(): Int = if (conf.implicitIntegerConversion && reader.currentBsonType == BsonType.INT64) {
        val number = decodeBsonElement(reader::readInt64, String::toLong)
        if (number >= Int.MIN_VALUE && number <= Int.MAX_VALUE) {
            number.toInt()
        } else {
            throw BsonDecodingException("Cannot cast INT64 to Int. Number is out of range: $number")
        }
    } else {
        decodeBsonElement(reader::readInt32, String::toInt)
    }

    override fun decodeLong(): Long = when (useMapper) {
        BsonKind.DATE -> decodeBsonDateTimeToLong()
        else -> if (conf.implicitIntegerConversion && reader.currentBsonType == BsonType.INT32) {
            decodeBsonElement(reader::readInt32, String::toInt).toLong()
        } else {
            decodeBsonElement(reader::readInt64, String::toLong)
        }
    }

    override fun decodeNull(): Nothing? = decodeBsonElement(
        { null },
        { null }
    )

    override fun decodeShort(): Short = decodeInt().toShort()

    override fun decodeString(): String = when (useMapper) {
        BsonKind.UUID -> decodeUUID().toString()
        BsonKind.OBJECT_ID -> decodeBsonObjectId()
        BsonKind.DATE -> decodeBsonDateTimeToString()
        else -> decodeBsonElement(reader::readString) { it }
    }

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int =
        enumDescriptor.getElementIndex(decodeString())

    fun decodeByteArray(): ByteArray {
        val binary = decodeBsonElement(reader::readBinaryData) { BsonBinary(it.toByteArray()) }
        return binary.data
    }

    fun decodeBsonDateTimeToLong(): Long = decodeBsonElement(reader::readDateTime, String::toLong)

    fun decodeBsonDateTimeToString(): String = Instant.ofEpochMilli(decodeBsonDateTimeToLong()).toString()

    fun decodeBsonObjectId(): String {
        val objectId = decodeBsonElement(reader::readObjectId) { ObjectId(it) }
        return objectId.toHexString()
    }

    fun decodeUUID(): UUID {
        val binary = decodeBsonElement(reader::readBinaryData) { BsonBinary(it.toByteArray()) }
        return binary.asUuid()
    }

    private fun <T> decodeBsonElement(readOps: () -> T, parse: (String) -> T): T {
        if (reader.state == INITIAL) {
            throw BsonDecodingException("BSON document cannot be decoded to a primitive type")
        }
        return when (state) {
            DecoderState.MAP_KEY -> {
                state = DecoderState.MAP_VALUE
                parse(reader.readName())
            }

            DecoderState.MAP_VALUE -> {
                state = DecoderState.MAP_KEY
                readOps()
            }

            else -> readOps()
        }
    }

    internal enum class DecoderState {
        DOCUMENT,
        LIST,
        MAP_KEY,
        MAP_VALUE,
        POLYMORPHIC,
    }

    private fun preserveState() {
        val currentState = if (state == DecoderState.MAP_VALUE) {
            DecoderState.MAP_KEY // swap state so we return to key after composite value
        } else {
            state
        }
        stateStack.addLast(currentState to currentIndex)
    }

    private fun restoreState() {
        with(stateStack.removeLast()) { // Restore state
            state = this.first
            currentIndex = this.second
        }
    }
}
