import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    `maven-publish`
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    implementation(kotlin("stdlib"))
    implementation("com.nimbusds:nimbus-jose-jwt:${Version.NIMBUS_JOSE_JWT}")
    implementation("com.google.guava:guava:${Version.GUAVA}-jre")

    compileOnly("org.springframework.boot:spring-boot:${Version.SPRING_BOOT}")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure:${Version.SPRING_BOOT}")
    compileOnly("org.apache.tomcat.embed:tomcat-embed-core:${Version.TOMCAT_EMBED}")
    compileOnly("org.springframework.security:spring-security-config:${Version.SPRING_SECURITY}")
    compileOnly("org.springframework.security:spring-security-web:${Version.SPRING_SECURITY}")
    compileOnly("org.springframework.security:spring-security-oauth2-client:${Version.SPRING_SECURITY}")
}

tasks.test {
    useJUnitPlatform()
}
