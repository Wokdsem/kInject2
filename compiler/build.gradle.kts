plugins {
    id("kinject.kotlin-library-conventions")
    kotlin("jvm")
    kotlin("kapt")
    `java-library`
}

kotlin { explicitApi() }
java { toolchain { languageVersion = JavaLanguageVersion.of(8) } }

tasks.test { useJUnitPlatform() }

dependencies {
    api(project(":kinject"))
    implementation("com.google.devtools.ksp:symbol-processing-api:2.0.21-1.0.25")
    implementation("com.squareup:kotlinpoet:1.18.1")
    implementation("com.squareup:kotlinpoet-ksp:1.18.1")

    compileOnly("com.google.auto.service:auto-service:1.1.1")
    kapt("com.google.auto.service:auto-service:1.1.1")

    testImplementation("org.jetbrains.kotlin:kotlin-reflect:2.0.21")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.2")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.6.0")
}
