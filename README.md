![Build and Test](https://github.com/imotions/bson4k/workflows/Build%20and%20Test/badge.svg)

# BSON 4K

[BSON](http://bsonspec.org/) is a binary JSON format native to [MongoDB](https://mongodb.com). BSON4K is an attempt to
implement support for the BSON format for Kotlinx Serialization in an idiomatic way.

## Features

### Encoding to `BsonDocument` or JSON string

Any complex type annotated as `@Serializable` can be serialized to `BsonDocument`,
the preferred format by MongoDB drivers, or a JSON string.
By complex, we mean a class-like reference type; the BSON spec makes a document the root
entity thus it makes no sense to support top-level serialization of primitives, i.e. Kotlin basic types.

### Decoding from `BsonDocument` or JSON string

Likewise, any BSON document can be deserialized into its Kotlin "serializable" object counterpart.
Alternatively, a JSON string can be parsed as long as it is valid BSON.

### Serializer-free type mapping for primitives

We introduce a type-mapping feature that allows you to work with clean `PrimitiveKind` serializers and map these to a
specific BSON type at runtime with minimal overhead (e.g. for UUIDs).
This is meant as a supplement to the existing extendability options of Kotlinx Serialization,
especially when working with a multiplatform library that brings its own serializable types accompanied by
format-agnostic serializers.

One of the strengths of Kotlin's serialization runtime is its extendability. If a type is not supported you can very
easily write a `KSerializer<T>` for that type. Kotlin's multiplatform primitive types are the only types supported by the
`Encoder`/`Decoder` interfaces which makes perfect sense, since multiplatform support is a top priority.

However, this leads to many developers writing custom serializers for common unsupported types
(e.g. UUID, LocalDateTime, etc.) which map to a supported native type in the serialization format. While
convenient this comes with drawbacks:

- The serializer must depend on a concrete encoder or decoder that supports serialization to/from these native types.
- If you consume a (possibly multiplatform) library that already provides serializers for its types, you are required
  to implement mapping types that are a copy/paste from the library types. Just in order to use your own serializers.

Serializers module configuration can mitigate these issues to some degree, but it quickly becomes difficult to manage.

## Usage

### Configuration

```kotlin
val bson = Bson {} // Default configuration

val bson = Bson {
    classDiscriminator = "<type>" // Set the class discriminator (field name to hold the type)
    serializersModule = mySerializersModule // Configure serializers module .
    addTypeMapping(MyUUIDSerializer, BsonKind.UUID) // Add a type mapping. Can be called multiple times.
    allowStructuredMapKeys = true // Enable serialization of complex map keys using arrays ([k, v...kn, vn])
}
```
| Configuration parameter  | Optional | Default  | Accepted input                                     |
| ------------------------ | -------- | -------- | -------------------------------------------------- |
| `serializersModule`      | yes      | None     | T : SerializersModule                              |
| `classDiscriminator`     | yes      | "__type" | String (avoid leading reserved bson chars like `$` |
| `addTypeMapping`         | yes      | None.    | `addTypeMapping(Serializer, BsonKind)`             |
| `allowStructuredMapKeys` | yes      | `false`  | Boolean                                            |

### Serialization

```kotlin
@Serializable
data class Person(val name: String, val age: Int)

val person = Person("Alan", 42)

val doc = bson.encodeToBsonDocument(person)

val json = bson.encodeToString(person) // Encode directly to extended JSON.
```

### Deserialization

```kotlin
val person = bson.decodeFromBsonDocument<Person>(doc)

// val person = bson.decodeFromString<Person>(json)
```

### Type mappers

```kotlin
import java.util.*

// UUID serializer implemented as a format-agnostic primitive kind serializer (String).
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

## Supported types, type mapping and `BsonKind`

### Supported BSON types

| BSON Type | Kotlin/Java Type        |
| --------- | ----------------------- |
| Document  | org.bson.Document       |
| Array     | List                    |
| Date      | java.time.Instant       |
| Boolean   | Boolean                 |
| Double    | Double                  |
| Int32     | Int                     |
| Int64     | Long                    |
| String    | String                  |
| Binary    | org.bson.types.Binary   |
| ObjectId  | org.bson.types.ObjectId |
| Null      | null                    |

### Basic type conversions

| Kotlin Type | BSON Type |
| ----------- | --------- |
| Byte        | Int32     |
| Char        | String    |
| Short       | Int32     |
| Float       | Double    |

### Type mappings

Sometimes it is beneficial to use a primitive-kind serializer while still using a more specialized format type. This
can be achieved via the type mapping feature. The possible target BSON types are specified in the `BsonKind` enum:

| BsonKind | Supported primitive kind(s) | Accepted input -> output    | BSON Type used     |
| -------- | --------------------------- | --------------------------- | ------------------ |
| UUID     | String                      | UUID string representation  | Binary UUID type 4 |
| DATE     | String, Long                | ISO String or Epoch ms Long | Date               |
| ObjectId | String                      | ObjectId hex String         | ObjectId           |

## Acknowledgements

**KBson**: A great many thanks to [jershell](https://github.com/jershell) for
his [kbson](https://github.com/jershell/kbson)
library. The code has provided a lot of inspiration. It was considered to fork and provide PRs for kbson, but for
several reasons we wanted to start over: getting our hands dirty, avoid contextual serialization and being able to quickly
adapt this library to our own needs.

**avro4K**: Beside being an awesome library that brings in Avro support in Kotlinx serialization, this project also
provided insights on implementing a custom serialization format in abundance.

## References

- Kotlinx Serialization: https://github.com/Kotlin/kotlinx.serialization
- BSON: http://bsonspec.org/
- MongoDB: https://mongodb.com
- MongoDB JVM BSON docs: https://mongodb.github.io/mongo-java-driver/4.2/bson/
