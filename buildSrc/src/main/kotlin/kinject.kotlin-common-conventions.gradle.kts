repositories {
    mavenCentral()
}

group = "com.wokdsem.kinject"
version = project.property("version") as String

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> { kotlinOptions.jvmTarget = "1.8" }