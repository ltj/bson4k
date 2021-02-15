package io.imotions.bson4k.encoder

import io.imotions.bson4k.common.Class2
import io.imotions.bson4k.common.Class3
import io.imotions.bson4k.common.bson
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi
import org.bson.BsonInt32
import org.bson.BsonNull
import org.bson.BsonString

@ExperimentalSerializationApi
class BsonClassEncoderTest : StringSpec({
    "Encode class" {
        val clazz = Class2("Fred", 42)
        val document = bson.encodeToBsonDocument(clazz)
            .also { println(it.toJson()) }

        document.size shouldBeExactly 2
        document.keys shouldContainAll listOf("x", "y")
        document.getString("x") shouldBe BsonString(clazz.x)
        document.getInt32("y") shouldBe BsonInt32(clazz.y)
    }

    "Encode class with nested class" {
        val nested = Class2("Fred", 42)
        val clazz = Class2(1L, nested)

        val document = bson.encodeToBsonDocument(clazz)
            .also { println(it.toJson()) }

        document.keys shouldContainAll listOf("x", "y")
        document.getDocument("y").keys shouldContainAll listOf("x", "y")
    }

    "Encode multiple nested classe including embedded array" {
        val nested = Class2("Fred", 42)
        val clazz = Class3(1L, "This is a comment", nested)
        val set = Class2("This is content", setOf(clazz))

        val document = bson.encodeToBsonDocument(set)
            .also { println(it.toJson()) }

        document.keys shouldContainAll listOf("x", "y")
        document.getArray("y").size shouldBeExactly 1
        document.getArray("y")[0].isDocument shouldBe true
    }

    "Encode nullable fields" {
        val testClass = Class3<Long, Int?, Class2<String, Int>>(12L, null, Class2("xyz", -42))
        val document = bson.encodeToBsonDocument(testClass)
            .also { println(it.toJson()) }

        document["y"] shouldBe BsonNull()
    }
})
