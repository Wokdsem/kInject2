plugins {
    kotlin("jvm")
    kotlin("kapt")
    alias(libs.plugins.mavenPublish)
}

kotlin { explicitApi() }
java { toolchain { languageVersion = JavaLanguageVersion.of(8) } }

tasks.test { useJUnitPlatform() }

dependencies {
    api(project(":kinject"))
    implementation(libs.ksp)
    implementation(libs.kotlinPoet)
    implementation(libs.kotlinPoetKsp)

    compileOnly(libs.autoService)
    kapt(libs.autoService)

    testImplementation(libs.kotlinReflect)
    testImplementation(libs.jUnitJupiter)
    testImplementation(libs.jUnitJupiterApi)
    testImplementation(libs.kspTest)
}
