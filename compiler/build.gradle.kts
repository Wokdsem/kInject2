plugins {
    id("kinject.kotlin-library-conventions")
    kotlin("jvm")
    kotlin("kapt")
    `java-library`
}

kotlin { explicitApi() }
tasks.test { useJUnitPlatform() }

dependencies {
    api(project(":kinject"))
    implementation("com.google.devtools.ksp:symbol-processing-api:1.8.22-1.0.11")
    implementation("com.squareup:kotlinpoet:1.14.2")
    implementation("com.squareup:kotlinpoet-ksp:1.14.2")

    compileOnly("com.google.auto.service:auto-service:1.0.1")
    kapt("com.google.auto.service:auto-service:1.0.1")

    testImplementation("org.jetbrains.kotlin:kotlin-reflect:1.7.10")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.4.9")
}
