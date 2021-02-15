# BSON 4K

[BSON](http://bsonspec.org/) is a binary Json format native to [MongoDB](https://mongodb.com). BSON4K is an attempt to
implement support for the BSON format for Kotlinx Serialization in an idiomatic way.

## Features

### Encoding to `BsonDocument` or Json string

Any complex type annotated as `@Serializabla` can be serialized to `BsonDocument` (preferred for e.g. MongoDB driver)
or directly to a json string. By complex, we mean a class-like reference type; the BSON spec makes a document the root
entity it makes no sense to support direct serialization of primitives i.e. Kotlin basic types.

### Decoding from `BsonDocument` or Json string

Likewise, any BSON document can be deserialized into it's Kotlin ("serializable") object counterpart. Alternatively a
json string can be parsed as long as it is valid BSON.

### Serializer-free type mapping for primitives

We introduce a type mapping feature that allows you to work with clean `PrimitiveKind` serializers and map these to a
specific BSON type at runtime with minimal overhead. It is meant as a supplement to the existing extendability options,
especially when working with a multi-platform library that brings its own serializable types accompanied by
format-agnostic serializers.

One of the strengths of Kotlin's serialization runtime is its extendability. If a type is not supported you can very
easily write a `KSerializer<T>` for that type. Kotlin's primitive types are the only types supported by the
`Encoding`/`Decoding` interfaces which makes perfect sense, since multi-platform support is a priority.

However, this leads to many developers writing contextual serializers for different common types
(e.g. UUID, LocalDateTime etc.) that maps to a more "appropriate" native type in the serialization format. While
convenient this comes with some drawbacks:

- The serializer must depend on a concrete encoder or decoder that supports serialization to/from these native types.
- If you consume a (maybe multi-platform) library that already provides serializers for its types, you would be required
  to implement mapping types that are copy-paste from the library types. Just in order to use your own serializers.

Serializers module configuration can mitigate these issues to some degree, but it quickly becomes difficult to manage.

## Usage

### Configuration

```kotlin
val bson = Bson {} // Default configuration

val bson = Bson {
    classDiscriminator = "<type>" // Set the class discriminator
    serializersModule = mySerializersModule // Configure serializers module 
    addTypeMapping(MyUUIDSerializer, BsonKind.UUID) // Add a type mapping. Can be called multiple times
}
```

### Serialization

```kotlin
@Serializable
data class Person(val name: String, val age: Int)

val person = Person("Alan", 42)

val doc = bson.encodeToBsonDocument(person)

val json = bson.encodeToString(person) // directly to extended Json
```

### Deserialization

```kotlin
val person = bson.decodeFromBsonDocument<Person>(doc)

// val person = bson.decodeFromString<Person>(json)
```

### Type Mappers

```kotlin
import java.util.*

// UUID serializer implemented as a format-agnostic primitive kind serializer (String) 
object UUIDSerializer : KSerializer<UUID> {
    override fun deserialize(decoder: Decoder): UUID =
        UUID.fromString(decoder.decodeString())

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("io.imotions.bson4k.uuid", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UUID) =
        encoder.encodeString(value.toString())
}

val bson = Bson {
    // Add the type mapping. This will ensure that even though UUID is to be encoded/decoded as String value,
    // it will be mapped more appropriately to BsonBinary with UUID data and standard representation. If left 
    // out it simply uses the serializer kind and becomes a BsonString.
    addTypeMapping(UUIDSerializer, BsonKind.UUID)
}

@Serializable
data class User(@Serializable(with = UUIDSerializer::class) val id: UUID, val name: String)

val user = User(UUID.randomUUID(), "Ada")

val document = bson.encodeToBsonDocument(user)

// Output will look like {"id": {"$binary": {"base64": "QGmcyx/LQKauHvFuGMcQDA==", "subType": "04"}}, "name": "Ada"}
```

## Type mapping, supported types and `BsonKind`

Coming soon.

## Acknowledgements

**KBson**: A great many thanks to [jershell](https://github.com/jershell) for
his [kbson](https://github.com/jershell/kbson)
library. The code has provided a lot of inspiration. It was considered to fork and provide PRs for kbson, but for
several reasons we wanted to start over: Getting our hands dirty, avoid contextual serialization and be able to quickly
adapt this library to our own needs.

**avro4K**: Beside being an awesome library that brings in Avro support in Kotlinx serialization, this project also
provided insights on implementing a custom serialization format in abundance.

## References

- Kotlinx Serialization: https://github.com/Kotlin/kotlinx.serialization
- BSON: http://bsonspec.org/
- MongoDB: https://mongodb.com
- MongoDB JVM BSON docs: https://mongodb.github.io/mongo-java-driver/4.2/bson/