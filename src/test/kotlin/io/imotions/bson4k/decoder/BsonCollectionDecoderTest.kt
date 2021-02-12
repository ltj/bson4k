package io.imotions.bson4k.decoder

import io.imotions.bson4k.common.bson
import io.imotions.bson4k.encoder.CollectionWrapper
import io.imotions.bson4k.encoder.TestItem
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
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
        val document = Document("collection", list.map { Document("name", it.toString()) })
        println(document.toJson())
        val wrapper = bson.decodeFromBsonDocument<CollectionWrapper<TestItem>>(document.toBsonDocument())

        wrapper shouldBe CollectionWrapper(list.map { TestItem(it.toString()) })
    }

    "Decode collection of objects with collections" {
        @Serializable
        data class Inner(val list: List<TestItem>)

        val document = Document(
            "collection", listOf(
                Document("list", list.map { Document("name", it.toString()) }),
                Document("list", list.map { Document("name", it.toString()) })
            )
        )
        println(document.toJson())

        val wrapper =
            bson.decodeFromBsonDocument<CollectionWrapper<Inner>>(document.toBsonDocument())
        val inner = Inner(list.map { TestItem(it.toString()) })
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