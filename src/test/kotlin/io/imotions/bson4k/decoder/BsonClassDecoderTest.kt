package io.imotions.bson4k.decoder

import io.imotions.bson4k.Bson
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import org.bson.Document

@ExperimentalSerializationApi
private val bson = Bson()

@Serializable
data class TestClass(val a: String, val b: Float)

@Serializable
data class NestingTestClass(val testClass: TestClass)

@ExperimentalSerializationApi
class BsonClassDecoderTest : StringSpec({
    "Decode simple class" {
        val document = Document("a", "hello").append("b", 34.5F).toBsonDocument()
        val testClass = bson.decodeFromBsonDocument<TestClass>(document)

        testClass shouldBe TestClass("hello", 34.5F)
    }

    "Decode simple class with fields reversed" {
        val document = Document("b", 34.5F).append("a", "hello").toBsonDocument()
        val testClass = bson.decodeFromBsonDocument<TestClass>(document)

        testClass shouldBe TestClass("hello", 34.5F)
    }

    "Decode nested class" {
        val document = Document("testClass", Document("a", "world").append("b", 10.45F))
        val nestingTestClass = bson.decodeFromBsonDocument<NestingTestClass>(document.toBsonDocument())

        nestingTestClass shouldBe NestingTestClass(TestClass("world", 10.45F))
    }
})
