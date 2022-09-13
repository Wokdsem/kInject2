
plugins {
    id("kinject.kotlin-application-conventions")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(project(":kinject"))
    ksp(project(":compiler"))
}

application {
    mainClass.set("com.wokdsem.kinject2.app.AppKt")
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}