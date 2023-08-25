plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.google.devtools.ksp") version "1.9.0-1.0.13"
}

kotlin {
    androidTarget()
    ios { binaries.framework { baseName = "shared" } }
}

android {
    compileSdk = 32
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 23
    }
    namespace = "com.wokdsem.kinject.app"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    sourceSets.commonMain { kotlin.srcDir("build/generated/ksp/metadata/commonmain/kotlin") }
}


dependencies {
    commonMainImplementation("com.wokdsem.kinject:kinject:2.1.2")
    add("kspCommonMainMetadata", "com.wokdsem.kinject:compiler:2.1.2")
}

afterEvaluate {
    tasks {
        withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>> {
            if (name != "kspCommonMainKotlinMetadata") dependsOn("kspCommonMainKotlinMetadata")
        }
    }
}