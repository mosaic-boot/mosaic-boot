plugins {
    kotlin("jvm") version Version.KOTLIN
}

val mosaicBootGroup = "io.mosaicboot"
val mosaicBootVersion = "0.0.1-SNAPSHOT"

group = mosaicBootGroup
version = mosaicBootVersion

allprojects {
    group = mosaicBootGroup
    version = mosaicBootVersion

    repositories {
        mavenCentral()
    }
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}