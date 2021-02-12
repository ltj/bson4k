package io.imotions.bson4k.encoder

import io.imotions.bson4k.common.bson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException

@Serializable
data class TestItem(val name: String)

@Serializable
data class CollectionWrapper<T>(val collection: Collection<T>)

@ExperimentalSerializationApi
class BsonCollectionEncoderTest : StringSpec({
    "Throw exception on attempting top-level array" {
        val list = listOf("one", "two", "three")
        shouldThrow<SerializationException> {
            bson.encodeToBsonDocument(list)
        }
    }

    "Encode wrapped primitive collection" {
        val list = listOf("one", "two", "three")
        val document = bson.encodeToBsonDocument(CollectionWrapper(list))
            .also { println(it) }
    }

    "Encode wrapped reference type collection" {
        val list = listOf(TestItem("one"), TestItem("two"), TestItem("three"))
        val document = bson.encodeToBsonDocument(CollectionWrapper(list))
            .also { println(it) }
    }

    "Encode collection of objects with collections" {
        val list = listOf(
            CollectionWrapper(listOf(12, 34, 56)),
            CollectionWrapper(listOf(78, 90))
        )
        val document = bson.encodeToBsonDocument(CollectionWrapper(list))
            .also { println(it) }
    }

    "Encode collection of collections" {
        val lists = listOf(
            listOf('a', 'b', 'c'),
            listOf('d')
        )
        val document = bson.encodeToBsonDocument(CollectionWrapper(lists)).also { println(it) }
    }
})
