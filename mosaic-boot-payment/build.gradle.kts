plugins {
    `java-library`
    id("com.vanniktech.maven.publish") version Version.VANNIKTECH_MAVEN_PUBLISH
    id("signing")
    kotlin("jvm") version Version.KOTLIN
    kotlin("plugin.spring") version Version.KOTLIN
    id("org.springframework.boot") version Version.SPRING_BOOT
    id("io.spring.dependency-management") version Version.SPRING_DEPENDENCY_MANAGEMENT
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjvm-default=all-compatibility")
    }
}

dependencies {
    api(project(":mosaic-boot-core"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.getByName<Jar>("bootJar") {
    enabled = false
}
