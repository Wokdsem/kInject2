
plugins {
    id("kinject2.kotlin-application-conventions")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(project(":kinject2"))
    ksp(project(":processor"))
}

application {
    mainClass.set("com.wokdsem.kinject2.app.AppKt")
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}