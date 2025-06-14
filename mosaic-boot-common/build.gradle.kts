import com.vanniktech.maven.publish.SonatypeHost

plugins {
    `java-library`
    id("com.vanniktech.maven.publish") version Version.VANNIKTECH_MAVEN_PUBLISH
    id("signing")
    kotlin("jvm") version Version.KOTLIN
    kotlin("plugin.spring") version Version.KOTLIN
    id("org.springframework.boot") version Version.SPRING_BOOT
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("plugin.allopen") version Version.KOTLIN
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
    api("org.springframework.data:spring-data-commons")

    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("org.jetbrains.kotlin:kotlin-reflect")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.getByName<Jar>("bootJar") {
    enabled = false
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    pom {
        name.set(project.name)
        description.set("mosaic-boot")
        url.set("https://mosaicboot.io")
        licenses {
            license {
                name.set("The Apache Software License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("jclab")
                name.set("Joseph Lee")
                email.set("joseph@jc-lab.net")
            }
        }
        scm {
            connection.set("scm:git:https://github.com/mosaic-boot/mosaic-boot.git")
            developerConnection.set("scm:git:ssh://git@github.com/mosaic-boot/mosaic-boot.git")
            url.set("https://github.com/mosaic-boot/mosaic-boot")
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

tasks.withType<Sign>().configureEach {
    onlyIf { project.hasProperty("signing.gnupg.keyName") || project.hasProperty("signing.keyId") }
}

