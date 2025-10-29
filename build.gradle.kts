import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.spring") version "1.9.24"
}

group = "ru.ai.assistant"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Web / реактивный стек
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    // R2DBC (runtime доступ к БД)
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    // Health/metrics endpoints
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Драйвер PostgreSQL для R2DBC
//    implementation("org.postgresql:r2dbc-postgresql:1.0.7.RELEASE")
    implementation("org.postgresql:r2dbc-postgresql")
//    implementation("io.r2dbc:r2dbc-postgresql:0.8.13.RELEASE")

    // Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    // Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")



    // Логи (входит в starter), оставляю явно для наглядности
//    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")

    // (опционально) логирование SQL
//    implementation("io.r2dbc:r2dbc-proxy:1.1.3.RELEASE")
//    implementation("org.slf4j:slf4j-api:2.0.13")

    // Liquibase (миграции) — работает через JDBC
    implementation("org.liquibase:liquibase-core")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    runtimeOnly("org.postgresql:postgresql")

    // Тесты
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")


    // Tests: Testcontainers
    testImplementation("org.testcontainers:testcontainers:1.20.2")
    testImplementation("org.testcontainers:postgresql:1.20.2")
    testImplementation("org.testcontainers:junit-jupiter:1.20.2")


    // mockk
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("io.mockk:mockk:1.13.17")
    testImplementation("com.ninja-squad:springmockk:4.0.2")

    // MockWebServer
    testImplementation("com.squareup.okhttp3:okhttp:4.12.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}
