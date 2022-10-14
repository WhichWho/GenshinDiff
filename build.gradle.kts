import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.4.30"
    application
}

group = "loli.ball"
version = "1.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes(mapOf("Main-Class" to "MainKt"))
    }
    from(configurations.runtimeClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    })
    val sourcesMain = sourceSets.main.get()
    sourcesMain.allSource.forEach { println("add from sources: ${it.name}") }
    from(sourcesMain.output)
}