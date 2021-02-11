package io.imotions.bson4k

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import org.bson.BsonInt32
import org.bson.BsonString

class BsonTest: StringSpec() {
    private val bson = Bson()

    init {
        "Encoder should encode data class of primitive types" {
            @Serializable
            data class Person(
                val name: String,
                val age: Int
            )

            val document = bson.encodeToBsonDocument(Person("Fred", 42))
                .also { println(it.toJson()) }

            document.keys shouldContainAll listOf("name", "age")
            document.getString("name") shouldBe BsonString("Fred")
            document.getInt32("age") shouldBe BsonInt32(42)
        }
    }

}