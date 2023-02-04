plugins {
    id("kinject.kotlin-library-conventions")
    kotlin("multiplatform")
}

kotlin {

    explicitApi()

    jvm()

    js(IR) {
        browser()
        nodejs()
    }

    macosX64()
    iosArm64()
    iosSimulatorArm64()
    iosX64()

    watchosArm32()
    watchosArm64()
    watchosSimulatorArm64()
    watchosX64()

    tvosArm64()
    tvosX64()
    tvosSimulatorArm64()

    linuxX64()

    mingwX64()

}
