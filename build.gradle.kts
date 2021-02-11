plugins {
    kotlin("jvm") version "1.4.30"
    kotlin("plugin.serialization") version "1.4.30"
}

group = "io.imotions.bson4k"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.1")
    implementation("org.mongodb:bson:4.2.0")

    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
    testImplementation("io.kotest:kotest-runner-junit5:4.4.1")
    testImplementation("io.kotest:kotest-assertions-core:4.4.1")
    testImplementation("io.kotest:kotest-property:4.4.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}