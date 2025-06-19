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

allOpen {
    annotations(
        "io.mosaicboot.core.http.MosaicController",
        "org.aspectj.lang.annotation.Aspect",
    )
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
    // Spring Boot
    api("org.springframework.data:spring-data-commons")
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-security")
    api("org.springframework.boot:spring-boot-starter-graphql")
    api("org.springframework.security:spring-security-oauth2-core")
    api("org.springframework.boot:spring-boot-starter-aop")
    compileOnly("org.springframework.security:spring-security-oauth2-client")

    api(project(":mosaic-boot-common"))

    implementation("org.springframework:spring-tx")
    implementation("com.fasterxml.uuid:java-uuid-generator:5.1.0")

    // Kotlin
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("org.jetbrains.kotlin:kotlin-reflect")

    // Auth
    implementation("com.nimbusds:nimbus-jose-jwt:10.0.1")
    // Bouncy Castle for cryptographic operations
    implementation("org.bouncycastle:bcprov-${Version.BCPROV}")
    implementation("org.bouncycastle:bcpkix-${Version.BCPROV}")

    // GraphQL
    api("com.graphql-java:graphql-java-extended-scalars:22.0")
    api("com.graphql-java-generator:graphql-java-common-runtime:2.8")

    // OpenAPI
    api("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.0")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.graphql:spring-graphql-test")

    testImplementation("org.springframework.boot:spring-boot-starter-oauth2-client")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
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
