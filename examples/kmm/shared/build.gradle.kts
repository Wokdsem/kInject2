plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.google.devtools.ksp") version "1.7.10-1.0.6"
}

kotlin {
    android()
    ios { binaries.framework { baseName = "shared" } }
}

android {
    compileSdk = 32
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 23
        targetSdk = 32
    }
}

kotlin {
    sourceSets.commonMain { kotlin.srcDir("build/generated/ksp/") }
}


dependencies {
    commonMainImplementation("com.wokdsem.kinject:kinject:2.0.0")
    add("kspCommonMainMetadata", "com.wokdsem.kinject:compiler:2.0.0")
}

afterEvaluate {
    tasks {
        withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>> {
            if (name != "kspCommonMainKotlinMetadata") dependsOn("kspCommonMainKotlinMetadata")
        }
    }
}