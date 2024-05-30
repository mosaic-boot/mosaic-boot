plugins {
    `java-library`
    `maven-publish`
    kotlin("jvm")
    kotlin("plugin.allopen") version Version.KOTLIN
}

repositories {
    mavenCentral()
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

allOpen {
    annotation("org.springframework.data.mongodb.core.mapping.Document")
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    implementation(kotlin("stdlib"))

    api(project(":mosaicboot-account"))
    implementation("com.nimbusds:nimbus-jose-jwt:${Version.NIMBUS_JOSE_JWT}")

    compileOnly("org.springframework.boot:spring-boot:${Version.SPRING_BOOT}")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure:${Version.SPRING_BOOT}")
    compileOnly("org.apache.tomcat.embed:tomcat-embed-core:${Version.TOMCAT_EMBED}")
    compileOnly("org.springframework.security:spring-security-config:${Version.SPRING_SECURITY}")
    compileOnly("org.springframework.security:spring-security-web:${Version.SPRING_SECURITY}")
    compileOnly("org.springframework.security:spring-security-oauth2-client:${Version.SPRING_SECURITY}")
    compileOnly("org.springframework.data:spring-data-mongodb:${Version.SPRING_DATA_MONGODB}")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}