import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.0"
    id("com.google.devtools.ksp") version "1.9.0-1.0.13"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.wokdsem.kinject:kinject:2.1.2")
    ksp("com.wokdsem.kinject:compiler:2.1.2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("com.wokdsem.kinject.app.AppKt")
}