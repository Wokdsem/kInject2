import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    id("com.google.devtools.ksp") version "1.7.10-1.0.6"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.wokdsem.kinject:kinject:2.0.0")
    ksp("com.wokdsem.kinject:compiler:2.0.0")
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("com.wokdsem.kinject.app.AppKt")
}