plugins {
    kotlin("jvm") version "1.4.30"
    kotlin("plugin.serialization") version "1.4.30"
    `maven-publish`
    id("io.gitlab.arturbosch.detekt").version("1.16.0-RC1")
}

group = "io.imotions.bson4k"
version = "0.1-RC1"

val ktlint by configurations.creating

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    ktlint("com.pinterest:ktlint:0.40.0")

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

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/imotions/bson4k")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        register("gpr", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
}

// Ktlint
val outputDir = "${project.buildDir}/reports/ktlint/"
val inputFiles = project.fileTree(mapOf("dir" to "src", "include" to "**/*.kt"))

val ktlintCheck by tasks.registering(JavaExec::class) {
    inputs.files(inputFiles)
    outputs.dir(outputDir)

    description = "Check Kotlin code style."
    classpath = ktlint
    main = "com.pinterest.ktlint.Main"
    args = listOf("--disabled_rules=no-wildcard-imports", "src/**/*.kt")
}

val ktlintFormat by tasks.registering(JavaExec::class) {
    inputs.files(inputFiles)
    outputs.dir(outputDir)

    description = "Fix Kotlin code style deviations."
    classpath = ktlint
    main = "com.pinterest.ktlint.Main"
    args = listOf("-F", "--disabled_rules=no-wildcard-imports", "src/**/*.kt")
}

tasks.detekt {
    dependsOn("ktlintCheck")
}