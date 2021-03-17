/*
 * Copyright 2021 iMotions A/S
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

import io.imotions.bson4k.common.EnumClass
import io.imotions.bson4k.common.Wrapper
import io.imotions.bson4k.common.Wrapper2
import io.imotions.bson4k.common.bson
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi
import org.bson.Document

@ExperimentalSerializationApi
class BsonClassDecoderTest : StringSpec({
    "Decode simple class" {
        val document = Document("x", "hello").append("y", 34.5F).toBsonDocument()
        val testClass = bson.decodeFromBsonDocument<Wrapper2<String, Float>>(document)

        testClass shouldBe Wrapper2("hello", 34.5F)
    }

    "Decode simple class with fields reversed" {
        val document = Document("y", 34.5F).append("x", "hello").toBsonDocument()
        val testClass = bson.decodeFromBsonDocument<Wrapper2<String, Float>>(document)

        testClass shouldBe Wrapper2("hello", 34.5F)
    }

    "Decode nested class" {
        val document = Document("value", Document("x", "world").append("y", 10.45F))
        val nestingTestClass = bson.decodeFromBsonDocument<Wrapper<Wrapper2<String, Float>>>(document.toBsonDocument())

        nestingTestClass shouldBe Wrapper(Wrapper2("world", 10.45F))
    }

    "Decode enum field" {
        val doc = Document("x", "enum fields").append("y", "FOURTH")
        val wrapper = bson.decodeFromBsonDocument<Wrapper2<String, EnumClass>>(doc.toBsonDocument())
        wrapper shouldBe Wrapper2("enum fields", EnumClass.FOURTH)
    }
})
