package io.imotions.bson4k.decoder

import io.imotions.bson4k.BsonConf
import io.imotions.bson4k.BsonKind
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.modules.SerializersModule
import org.bson.AbstractBsonReader
import org.bson.AbstractBsonReader.State.*
import org.bson.BsonBinary
import org.bson.BsonType
import org.bson.types.ObjectId
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
    private var currentIndex = -1
    private var useMapper: BsonKind = BsonKind.PASS_THROUGH

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        return when (state) {
            DecoderState.DOCUMENT -> {
                if (reader.state == TYPE) {
                    reader.readBsonType()
                }
                if (reader.state == NAME) {
                    return descriptor.getElementIndex(reader.readName())
                }
                DECODE_DONE
            }
            DecoderState.LIST -> {
                val type = reader.readBsonType()
                if (type == BsonType.END_OF_DOCUMENT) DECODE_DONE else ++currentIndex
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
                            throw SerializationException(
                                "Unknown class discriminator. Expected \"${conf.classDiscriminator}\""
                            )
                        }
                    }
                    1 -> currentIndex++
                    else -> DECODE_DONE
                }
            }
        }
    }

    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>, previousValue: T?): T {
        useMapper = conf.bsonTypeMappings.getOrDefault(deserializer.descriptor.serialName, BsonKind.PASS_THROUGH)
        return super.decodeSerializableValue(deserializer, previousValue)
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        preserveState()
        when (descriptor.kind) {
            StructureKind.LIST -> {
                state = DecoderState.LIST
                currentIndex = -1
                reader.readStartArray()
            }
            StructureKind.MAP -> {
                state = DecoderState.MAP_KEY
                reader.readStartDocument()
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
        }
        return super.beginStructure(descriptor)
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        restoreState()
        when (descriptor.kind) {
            StructureKind.LIST -> {
                reader.readEndArray()
            }
            is StructureKind -> {
                reader.readEndDocument()
            }
        }
        super.endStructure(descriptor)
    }

    override fun decodeNotNullMark(): Boolean {
        return reader.currentBsonType != BsonType.NULL
    }

    override fun decodeBoolean(): Boolean = decodeBsonElement(reader::readBoolean, String::toBoolean)

    override fun decodeByte(): Byte = decodeInt().toByte()

    override fun decodeChar(): Char = decodeString().first()

    override fun decodeDouble(): Double = decodeBsonElement(reader::readDouble, String::toDouble)

    override fun decodeFloat(): Float = decodeDouble().toFloat()

    override fun decodeInt(): Int = decodeBsonElement(reader::readInt32, String::toInt)

    override fun decodeLong(): Long = when (useMapper) {
        BsonKind.DATE -> decodeBsonDateTime()
        else -> decodeBsonElement(reader::readInt64, String::toLong)
    }

    override fun decodeNull(): Nothing? = decodeBsonElement(
        {
            reader.readNull()
            null
        },
        { null }
    )

    override fun decodeShort(): Short = decodeInt().toShort()

    override fun decodeString(): String = when (useMapper) {
        BsonKind.UUID -> decodeUUID().toString()
        BsonKind.OBJECT_ID -> decodeBsonObjectId()
        else -> decodeBsonElement(reader::readString) { it }
    }

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int =
        enumDescriptor.getElementIndex(decodeString())

    fun decodeByteArray(): ByteArray {
        val binary = decodeBsonElement(reader::readBinaryData) { BsonBinary(it.toByteArray()) }
        return binary.data
    }

    fun decodeBsonDateTime(): Long = decodeBsonElement(reader::readDateTime, String::toLong)

    fun decodeBsonObjectId(): String {
        val objectId = decodeBsonElement(reader::readObjectId) { ObjectId(it) }
        return objectId.toHexString()
    }

    fun decodeUUID(): UUID {
        val binary = decodeBsonElement(reader::readBinaryData) { BsonBinary(it.toByteArray()) }
        return binary.asUuid()
    }

    private fun <T> decodeBsonElement(readOps: () -> T, fromString: (String) -> T): T {
        if (reader.state == INITIAL) {
            throw SerializationException("Bson document cannot be decoded to a primitive type")
        }
        return when (state) {
            DecoderState.MAP_KEY -> {
                state = DecoderState.MAP_VALUE
                fromString(reader.readName())
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
