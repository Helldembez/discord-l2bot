import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.21"
    application
}

group = "com.helldembez.discordl2bot"
version = "0.1"

repositories {
    mavenCentral()
}

val mockkVersion = "1.13.12"

dependencies {
    implementation("com.jessecorbett:diskord-bot:5.4.0")
    implementation("org.jsoup:jsoup:1.15.3")
    implementation("io.arrow-kt:arrow-core:1.2.4")
    implementation("io.ktor:ktor-client-core-jvm:2.3.7")
    implementation("io.ktor:ktor-client-apache:2.3.7")
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk-dsl-jvm:$mockkVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

application {
    mainClass.set("com.example.MainKt")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.helldembez.discordl2bot.MainKt"
    }
    from({
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })
    duplicatesStrategy = DuplicatesStrategy.WARN
}