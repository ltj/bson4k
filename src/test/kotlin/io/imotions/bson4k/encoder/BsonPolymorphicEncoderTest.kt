package io.imotions.bson4k.encoder

import io.imotions.bson4k.Bson
import io.imotions.bson4k.CLASS_DISCRIMINATOR
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@ExperimentalSerializationApi
private val bson = Bson()
private val json = Json { classDiscriminator = CLASS_DISCRIMINATOR }

@Serializable
data class VehicleIdentifier(val make: String, val model: String)

@Serializable
sealed class Vehicle {
    @Serializable
    sealed class Car : Vehicle() {
        @Serializable
        data class ElectricCar(val identifier: VehicleIdentifier, val maxDistance: Int) : Car()

        @Serializable
        data class HybridCar(
            val identifier: VehicleIdentifier,
            val maxDistanceBattery: Int,
            val avgEmissionKm: Double
        ) : Car()

        @Serializable
        data class CombustionEngineCar(val identifier: VehicleIdentifier, val avgEmissionKm: Double) : Car()
    }

    @Serializable
    sealed class Bike : Vehicle() {
        @Serializable
        data class NormalBike(val identifier: VehicleIdentifier, val weightGrams: Int) : Bike()

        @Serializable
        data class ElectricBike(val identifier: VehicleIdentifier, val topSpeed: Int) : Bike()
    }
}

@Serializable
data class CarWrapper(val car: Vehicle.Car)

@Serializable
data class VehicleCollectionWrapper(val name: String, val collection: List<Vehicle>)

@ExperimentalSerializationApi
class BsonPolymorphicEncoderTest : StringSpec({
    "Encode document using object polymorphism" {
        val car = Vehicle.Car.ElectricCar(VehicleIdentifier("Minion", "Tree"), 100)
        val document = bson.encodeToBsonDocument(Vehicle.Car.serializer(), car)
            .also { println(it.toJson()) }


        val serialized = json.decodeFromString(Vehicle.Car.serializer(), document.toJson())
        document.firstKey shouldBe CLASS_DISCRIMINATOR
        serialized shouldBe car
    }

    "Encode wrapped polymorphic type" {
        val car = Vehicle.Car.ElectricCar(VehicleIdentifier("Minion", "Tree"), 100)
        val wrapper = CarWrapper(car)
        val document = bson.encodeToBsonDocument(wrapper)
            .also { println(it.toJson()) }

        val deserialized = json.decodeFromString(CarWrapper.serializer(), document.toJson())
        deserialized shouldBe wrapper
    }

    "Encode wrapped polymorphic array" {
        val collection = listOf(
            Vehicle.Car.ElectricCar(VehicleIdentifier("Minion", "Tree"), 100),
            Vehicle.Car.HybridCar(VehicleIdentifier("Soya", "BetterWorld"), 30, 5.5),
            Vehicle.Car.CombustionEngineCar(VehicleIdentifier("Old world", "Smogger"), 12.7),
            Vehicle.Bike.NormalBike(VehicleIdentifier("Smartster", "Raze"), 3000),
            Vehicle.Bike.ElectricBike(VehicleIdentifier("E-life", "Modern"), 25)
        )
        val wrapper = VehicleCollectionWrapper("My collection of vehicles", collection)
        val document = bson.encodeToBsonDocument(wrapper)
            .also { println(it.toJson()) }

        val deserialized = json.decodeFromString(VehicleCollectionWrapper.serializer(), document.toJson())
        deserialized shouldBe wrapper
    }
})
