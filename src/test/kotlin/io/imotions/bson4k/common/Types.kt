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

package io.imotions.bson4k.common

import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import java.time.Instant
import java.util.*

// Polymorphic types

@Serializable
sealed class SealedClass {
    @Serializable
    data class PolyOne(val value: Int) : SealedClass()
    @Serializable
    data class PolyTwo(val value: String) : SealedClass()
    @Serializable
    data class PolyList(val list: List<PolyOne>) : SealedClass()
}

// Wrappers

@Serializable
data class CollectionWrapper<T>(val collection: Collection<T>)

@Serializable
data class Wrapper<T>(val value: T)

@Serializable
data class Wrapper2<A, B>(val x: A, val y: B)

@Serializable
data class MapWrapper<K, V>(val map: Map<K, V>)

// Classes

@Serializable
class Class2<A, B>(val x: A, val y: B)

@Serializable
class Class3<A, B, C>(val x: A, val y: B, val z: C)

@Serializable
class Class2Collection<A, B>(val x: A, val y: Collection<B>)

// Enums

@Serializable
enum class EnumClass {
    FIRST,
    SECOND,
    THIRD,
    FOURTH
}

// Type mapping

@Serializable
data class StringUUIDContainer(
    @Serializable(with = UUIDSerializer::class) val uuid: UUID
)

@Serializable
data class LongDateContainer(
    @Serializable(with = InstantLongSerializer::class) val date: Instant
)

@Serializable
data class StringDateContainer(
    @Serializable(with = InstantStringSerializer::class) val date: Instant
)

@Serializable
data class StringNullableDateContainer(
    @Serializable(with = InstantStringSerializer::class) val date: Instant?
)

@Serializable
data class StringObjectIdContainer(
    @Serializable(with = ObjectIdSerializer::class) val objectId: ObjectId
)

@Serializable
data class BsonTypesWithSerializers(
    @Serializable(with = UUIDSerializer::class) val uuid: UUID,
    @Serializable(with = InstantLongSerializer::class) val date: Instant,
    @Serializable(with = ObjectIdSerializer::class) val objectId: ObjectId
)

@Serializable
data class BsonSingleTypeWithSerializer(
    @Serializable(with = UUIDSerializer::class) val uuid: UUID,
    val x: String,
    val y: String
)
