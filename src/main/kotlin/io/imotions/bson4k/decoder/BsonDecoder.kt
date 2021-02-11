package io.imotions.bson4k.decoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.modules.SerializersModule
import org.bson.AbstractBsonReader
import org.bson.AbstractBsonReader.State.*
import org.bson.BsonType

@ExperimentalSerializationApi
class BsonDecoder(
    private val reader: AbstractBsonReader,
    override val serializersModule: SerializersModule
) : AbstractDecoder() {
    private var stateStack = ArrayDeque<Pair<DecoderState, Int>>()
    private var state = DecoderState.DOCUMENT
    private var currentIndex = -1

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
        }
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        stateStack.addLast(state to currentIndex) // Preserve state on new structure
        when (descriptor.kind) {
            StructureKind.LIST -> {
                reader.readStartArray()
                state = DecoderState.LIST
                currentIndex = -1
            }
            is StructureKind -> {
                state = DecoderState.DOCUMENT
                reader.readStartDocument()
            }
        }
        return super.beginStructure(descriptor)
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        with(stateStack.removeLast()) { // Restore state
            state = this.first
            currentIndex = this.second
        }
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

    override fun decodeBoolean(): Boolean = decodeBsonElement(reader::readBoolean)

    override fun decodeByte(): Byte = decodeInt().toByte()

    override fun decodeChar(): Char = decodeString().first()

    override fun decodeDouble(): Double = decodeBsonElement(reader::readDouble)

    override fun decodeFloat(): Float = decodeDouble().toFloat()

    override fun decodeInt(): Int = decodeBsonElement(reader::readInt32)

    override fun decodeLong(): Long = decodeBsonElement(reader::readInt64)

    override fun decodeNull(): Nothing? = decodeBsonElement {
        reader.readNull()
        null
    }

    override fun decodeShort(): Short = decodeInt().toShort()

    override fun decodeString(): String = decodeBsonElement(reader::readString)

    private fun <T> decodeBsonElement(readOps: () -> T): T {
        if (reader.state == INITIAL) {
            throw SerializationException("Bson document cannot be decoded to a primitive type")
        }
        return readOps()
    }

    internal enum class DecoderState {
        DOCUMENT,
        LIST
    }
}
