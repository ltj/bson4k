package io.imotions.bson4k.decoder

import io.imotions.bson4k.common.Wrapper
import io.imotions.bson4k.common.CollectionWrapper
import io.imotions.bson4k.common.bson
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi
import org.bson.Document

private val list = listOf(12, 34, 56, 78, 90)
private val nullableList = listOf(12, null, 56, 78, null)

@ExperimentalSerializationApi
class BsonCollectionDecoderTest : StringSpec({
    "Decode collection of values" {
        val document = Document("collection", list)
        println(document.toJson())
        val wrapper = bson.decodeFromBsonDocument<CollectionWrapper<Int>>(document.toBsonDocument())

        wrapper shouldBe CollectionWrapper(list)
    }

    "Decode collection of nullable values" {
        val document = Document("collection", nullableList)
        println(document.toJson())
        val wrapper = bson.decodeFromBsonDocument<CollectionWrapper<Int?>>(document.toBsonDocument())

        wrapper shouldBe CollectionWrapper(nullableList)
    }

    "Decode collection of objects" {
        val document = Document("collection", list.map { Document("value", it.toString()) })
        println(document.toJson())
        val wrapper = bson.decodeFromBsonDocument<CollectionWrapper<Wrapper<String>>>(document.toBsonDocument())

        wrapper shouldBe CollectionWrapper(list.map { Wrapper(it.toString()) })
    }

    "Decode collection of objects with collections" {
        val document = Document(
            "collection", listOf(
                Document("collection", list.map { Document("value", it.toString()) }),
                Document("collection", list.map { Document("value", it.toString()) })
            )
        )
        println(document.toJson())

        val wrapper =
            bson.decodeFromBsonDocument<CollectionWrapper<CollectionWrapper<Wrapper<String>>>>(document.toBsonDocument())
        val inner = CollectionWrapper(list.map { Wrapper(it.toString()) })
        wrapper shouldBe CollectionWrapper(listOf(inner, inner))
    }

    "Decode collection of collections" {
        val nestedLists = listOf(
            listOf(1, 2, 3),
            listOf(4, 5)
        )
        val document = Document("collection", nestedLists)

        val wrapper = bson.decodeFromBsonDocument<CollectionWrapper<List<Int>>>(document.toBsonDocument())
        wrapper shouldBe CollectionWrapper(nestedLists)
    }
})