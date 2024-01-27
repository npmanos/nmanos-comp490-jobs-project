plugins {
    kotlin("jvm") version "1.9.22"
}

group = "edu.bridgew.comp490"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.serpapi:google-search-results-java:aff4a9caf3")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.8.10")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
