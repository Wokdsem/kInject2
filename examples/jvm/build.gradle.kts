plugins {
    kotlin("jvm") version "2.0.21"
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
    application
}

application {
    mainClass.set("com.wokdsem.kinject.app.AppKt")
}

ksp {
    arg("kInject-graphDir", layout.projectDirectory.dir(".kinject").asFile.absolutePath)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.wokdsem.kinject:kinject:2.3.0")
    ksp("com.wokdsem.kinject:compiler:2.3.0")
}
