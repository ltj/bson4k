package io.imotions.bson4k.common

import kotlinx.serialization.Serializable

// Polymorphic types

@Serializable
sealed class SealedTest{
    @Serializable
    data class PolyOne(val value: Int) : SealedTest()
    @Serializable
    data class PolyTwo(val value: String) : SealedTest()
}

// Wrappers

@Serializable
data class CollectionWrapper<T>(val collection: Collection<T>)

@Serializable
data class BasicWrapper<T>(val value: T)
