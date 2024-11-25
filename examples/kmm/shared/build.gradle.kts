import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
}

kotlin {
    androidTarget()
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }
}

android {
    compileSdk = 34
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 23
    }
    namespace = "com.wokdsem.kinject.app"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

kotlin {
    sourceSets.commonMain { kotlin.srcDir("build/generated/ksp/metadata/commonmain/kotlin") }
}

ksp {
    arg("kInject-graphDir", layout.projectDirectory.dir(".kinject").asFile.absolutePath)
}

dependencies {
    commonMainImplementation("com.wokdsem.kinject:kinject:2.3.0")
    add("kspCommonMainMetadata", "com.wokdsem.kinject:compiler:2.3.0")
}

afterEvaluate {
    tasks {
        withType<KotlinCompilationTask<*>> {
            if (name != "kspCommonMainKotlinMetadata") dependsOn("kspCommonMainKotlinMetadata")
        }
    }
}