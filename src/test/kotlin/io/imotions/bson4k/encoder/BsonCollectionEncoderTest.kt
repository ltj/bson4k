package io.imotions.bson4k.encoder

import io.imotions.bson4k.common.CollectionWrapper
import io.imotions.bson4k.common.Wrapper
import io.imotions.bson4k.common.bson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.checkAll
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException

@ExperimentalSerializationApi
class BsonCollectionEncoderTest : StringSpec({
    "Throw exception on attempting top-level array" {
        val list = listOf("one", "two", "three")
        shouldThrow<SerializationException> {
            bson.encodeToBsonDocument(list)
        }
    }

    "Encode String collection" {
        checkAll<List<String>> { list ->
            bson.encodeToBsonDocument(CollectionWrapper(list))
        }
    }

    "Encode wrapped reference type collection" {
        checkAll<List<Double>> { list ->
            bson.encodeToBsonDocument(CollectionWrapper(list.map { Wrapper(it) }))
        }
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
