plugins {
    id("kinject.kotlin-library-conventions")
    kotlin("multiplatform")
}

kotlin {

    explicitApi()

    jvm()

    js {
        browser()
        nodejs()
    }

    macosX64()
    iosArm32()
    iosArm64()
    iosSimulatorArm64()
    iosX64()

    watchosArm32()
    watchosArm64()
    watchosSimulatorArm64()
    watchosX86()
    watchosX64()

    tvosArm64()
    tvosX64()
    tvosSimulatorArm64()

    linuxX64()
    linuxArm32Hfp()

    mingwX64()

}
