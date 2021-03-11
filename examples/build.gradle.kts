
plugins {
    id("kinject2.kotlin-application-conventions")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(project(":kinject2"))
    ksp(project(":processor"))
}

application {
    mainClass.set("kinject2.app.AppKt")
}
