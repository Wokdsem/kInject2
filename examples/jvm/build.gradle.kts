plugins {
    kotlin("jvm") version "2.0.21"
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.wokdsem.kinject:kinject:2.2.3")
    ksp("com.wokdsem.kinject:compiler:2.2.3")
}

application {
    mainClass.set("com.wokdsem.kinject.app.AppKt")
}