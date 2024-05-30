plugins {
    `java-library`
    `maven-publish`
    kotlin("jvm")
    kotlin("plugin.allopen") version Version.KOTLIN
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

    compileOnly("org.springframework.boot:spring-boot:${Version.SPRING_BOOT}")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure:${Version.SPRING_BOOT}")
    compileOnly("org.springframework:spring-web:${Version.SPRING_WEB}")
    compileOnly("org.springframework.data:spring-data-mongodb:${Version.SPRING_DATA_MONGODB}")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}