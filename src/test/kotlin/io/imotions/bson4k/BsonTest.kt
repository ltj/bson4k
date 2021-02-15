package io.imotions.bson4k

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.EmptySerializersModule
import java.lang.IllegalArgumentException

@ExperimentalSerializationApi
class BsonTest : StringSpec({
    "Builder should return Bson with default configuration" {
        val bson = Bson { }
        bson.configuration.classDiscriminator shouldBe CLASS_DISCRIMINATOR
        bson.configuration.serializersModule shouldBe EmptySerializersModule
    }

    "Builder should fail on invalid chars in class discriminator" {
        shouldThrow<IllegalArgumentException> {
            Bson { classDiscriminator = "\$type" }
        }

        shouldThrow<IllegalArgumentException> {
            Bson { classDiscriminator = "type.." }
        }
    }
})
