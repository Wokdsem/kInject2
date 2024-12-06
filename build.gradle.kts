
plugins {
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.toolchainResolver) apply false
    alias(libs.plugins.mavenPublish) apply false
}

group = "com.wokdsem.kinject"
version = project.property("version") as String

allprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
    }
    repositories {
        mavenCentral()
    }
}
