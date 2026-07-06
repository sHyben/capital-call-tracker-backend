import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.3.2"
	id("io.spring.dependency-management") version "1.1.5"
	kotlin("jvm") version "1.9.24"
	kotlin("plugin.spring") version "1.9.24"
	kotlin("plugin.jpa") version "1.9.24"
}

group = "com.swissre"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

val kotlinCoroutinesVersion = "1.8.1"
val mockkVersion = "1.13.11"
val springMockkVersion = "4.0.2"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-actuator")

	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinCoroutinesVersion")

	runtimeOnly("org.postgresql:postgresql")

	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(module = "mockito-core")
	}
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinCoroutinesVersion")
	testImplementation("io.mockk:mockk:$mockkVersion")
	testImplementation("com.ninja-squad:springmockk:$springMockkVersion")
	testImplementation("org.junit.jupiter:junit-jupiter")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
		jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
	}
}

tasks.withType<KotlinCompile> {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
		jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// Loads local secrets (DB credentials, Azure IDs) from a gitignored .env file so
// `./gradlew bootRun` works without exporting env vars by hand. In CI/production,
// the real env vars are set by the platform and .env is simply absent — this is a no-op then.
val dotenv: Map<String, String> = file(".env").let { envFile ->
	if (!envFile.exists()) emptyMap()
	else envFile.readLines()
		.map { it.trim() }
		.filter { it.isNotEmpty() && !it.startsWith("#") && it.contains("=") }
		.associate { line ->
			val (key, value) = line.split("=", limit = 2)
			key.trim() to value.trim()
		}
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
	environment(dotenv)
}
