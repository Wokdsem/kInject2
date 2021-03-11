
plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

val kotlinVersion = project.findProperty("kotlinVersion")
dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:$kotlinVersion"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}
