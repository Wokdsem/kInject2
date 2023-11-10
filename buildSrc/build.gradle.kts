plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20")
    implementation("com.vanniktech:gradle-maven-publish-plugin:0.25.2")
}
