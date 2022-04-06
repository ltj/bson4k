/*
 * Copyright 2022 iMotions A/S
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

package io.imotions.bson4k.decoder

import io.imotions.bson4k.common.CollectionWrapper
import io.imotions.bson4k.common.Wrapper
import io.imotions.bson4k.common.bson
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi
import org.bson.Document

private val list = listOf(12, 34, 56, 78, 90)
private val nullableList = listOf(12, null, 56, 78, null)
private val longList = listOf(12L, 34L, 56L, 78L, 90L)

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
            "collection",
            listOf(
                Document("collection", list.map { Document("value", it.toString()) }),
                Document("collection", list.map { Document("value", it.toString()) })
            )
        )
        println(document.toJson())

        val wrapper =
            bson.decodeFromBsonDocument<CollectionWrapper<CollectionWrapper<Wrapper<String>>>>(
                document.toBsonDocument()
            )
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

    "Decode collection with implicit integer conversion INT64 -> INT32" {
        val document = Document("collection", longList)
        println(document.toJson())
        val wrapper = bson.decodeFromBsonDocument<CollectionWrapper<Int>>(document.toBsonDocument())

        wrapper shouldBe CollectionWrapper(list)
    }

    "Decode collection with implicit integer conversion INT32 -> INT64" {
        val document = Document("collection", list)
        println(document.toJson())
        val wrapper = bson.decodeFromBsonDocument<CollectionWrapper<Long>>(document.toBsonDocument())

        wrapper shouldBe CollectionWrapper(longList)
    }
})
