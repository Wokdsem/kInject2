plugins {
    id("kinject2.kotlin-library-conventions")
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    api(project(":kinject2"))
    implementation("com.google.devtools.ksp:symbol-processing-api:1.7.10-1.0.6")
    implementation("com.squareup:kotlinpoet:1.12.0")
    implementation("com.squareup:kotlinpoet-ksp:1.12.0")
    implementation("org.junit.jupiter:junit-jupiter:5.8.1")
    implementation("org.junit.jupiter:junit-jupiter:5.8.1")

    testImplementation("org.jetbrains.kotlin:kotlin-reflect:1.7.10")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.4.9")
}
