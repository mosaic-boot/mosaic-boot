import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    kotlin("jvm") version Version.KOTLIN
    id("com.vanniktech.maven.publish") version Version.VANNIKTECH_MAVEN_PUBLISH apply false
}

repositories {
    mavenCentral()
}

group = "io.mosaicboot"
version = Version.PROJECT

subprojects {
    group = "io.mosaicboot"
    version = Version.PROJECT

    repositories {
        mavenCentral()
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

//    extensions.getByType(JavaPluginExtension::class).apply {
//        sourceCompatibility = JavaVersion.VERSION_17
//        targetCompatibility = JavaVersion.VERSION_17
//    }
}
