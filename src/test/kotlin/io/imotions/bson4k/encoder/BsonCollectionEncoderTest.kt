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
        bson.encodeToBsonDocument(CollectionWrapper(list))
            .also { println(it) }
    }

    "Encode collection of collections" {
        val lists = listOf(
            listOf('a', 'b', 'c'),
            listOf('d')
        )
        bson.encodeToBsonDocument(CollectionWrapper(lists)).also { println(it) }
    }
})
