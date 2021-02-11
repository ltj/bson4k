package io.imotions.bson4k.encoder

import io.imotions.bson4k.Bson
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.BsonInt32
import org.bson.BsonNull
import org.bson.BsonString

@Serializable
data class TestClassLv2(val x: String, val y: Int)

@Serializable
data class TestClassLv1(val x: Long, val y: String?, val testClassLv2: TestClassLv2)

@Serializable
data class TestClassTop(val x: String, val testClassLv1s: Set<TestClassLv1>)

@ExperimentalSerializationApi
private val bson = Bson()

@Serializable
object TestObject {
    const val a = 12
    const val b = "text"
}

@ExperimentalSerializationApi
class BsonClassEncoderTest : StringSpec({
    "Encode class" {
        val user = TestClassLv2("Fred", 42)
        val document = bson.encodeToBsonDocument(user)
            .also { println(it.toJson()) }

        document.size shouldBeExactly 2
        document.keys shouldContainAll listOf("x", "y")
        document.getString("x") shouldBe BsonString(user.x)
        document.getInt32("y") shouldBe BsonInt32(user.y)
    }

    "Encode class with nested class" {
        val testClassLv2 = TestClassLv2("Fred", 42)
        val testClassLv1 = TestClassLv1(1L, "This is a comment", testClassLv2)

        val document = bson.encodeToBsonDocument(testClassLv1)
            .also { println(it.toJson()) }

        document.keys shouldContainAll listOf("x", "y", "testClassLv2")
        document.getDocument("testClassLv2").keys shouldContainAll listOf("x", "y")
    }

    "Encode multiple nested classe including embedded array" {
        val testClassLv2 = TestClassLv2("Fred", 42)
        val testClassLv1 = TestClassLv1(1L, "This is a comment", testClassLv2)
        val testClassTop = TestClassTop("This is content", setOf(testClassLv1))

        val document = bson.encodeToBsonDocument(testClassTop)
            .also { println(it.toJson()) }

        document.keys shouldContainAll listOf("x", "testClassLv1s")
        document.getArray("testClassLv1s").size shouldBeExactly 1
        document.getArray("testClassLv1s").get(0).isDocument shouldBe true
    }

    "Encode nullable fields" {
        val testClass = TestClassLv1(12L, null, TestClassLv2("xyz", -42))
        val document = bson.encodeToBsonDocument(testClass)
            .also { println(it.toJson()) }

        document["y"] shouldBe BsonNull()
    }

    "Encode object" {
        val document = bson.encodeToBsonDocument(TestObject)
            .also { println(it.toJson()) }
    }
})