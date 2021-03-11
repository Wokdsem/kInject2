plugins {
    id("kinject2.kotlin-library-conventions")
}

dependencies {
    api(project(":kinject2"))
    implementation("com.google.devtools.ksp:symbol-processing-api:1.7.10-1.0.6")
}
