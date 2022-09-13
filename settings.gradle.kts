pluginManagement {
    val kotlinVersion: String by settings
    val kspVersion: String by settings
    plugins {
        id("com.google.devtools.ksp") version kspVersion
        kotlin("jvm") version kotlinVersion
    }
    repositories {
        mavenCentral()
    }
}

rootProject.name = "kinject2"

include("kinject", "compiler", "examples")

