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
    // Health/metrics endpoints
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    // Kotlin + Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    // Логи (входит в starter), оставляю явно для наглядности
//    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")

    // Драйвер PostgreSQL для R2DBC
    implementation("io.r2dbc:r2dbc-postgresql:0.8.13.RELEASE")
    // R2DBC (runtime доступ к БД)
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")

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
