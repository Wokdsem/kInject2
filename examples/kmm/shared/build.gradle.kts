plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.google.devtools.ksp") version "1.9.20-1.0.14"
}

kotlin {
    androidTarget()
    iosArm64()
    iosSimulatorArm64()
    iosX64()
}

android {
    compileSdk = 32
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 23
    }
    namespace = "com.wokdsem.kinject.app"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_19
        targetCompatibility = JavaVersion.VERSION_19
    }
}

kotlin {
    sourceSets.commonMain { kotlin.srcDir("build/generated/ksp/metadata/commonmain/kotlin") }
}


dependencies {
    commonMainImplementation("com.wokdsem.kinject:kinject:2.1.4")
    add("kspCommonMainMetadata", "com.wokdsem.kinject:compiler:2.1.4")
}

afterEvaluate {
    tasks {
        withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>> {
            if (name != "kspCommonMainKotlinMetadata") dependsOn("kspCommonMainKotlinMetadata")
        }
    }
}