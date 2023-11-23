import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.20"
    id("com.google.devtools.ksp") version "1.9.20-1.0.14"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.wokdsem.kinject:kinject:2.1.4")
    ksp("com.wokdsem.kinject:compiler:2.1.4")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("com.wokdsem.kinject.app.AppKt")
}