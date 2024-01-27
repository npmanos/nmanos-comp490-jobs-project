plugins {
    kotlin("jvm") version "1.9.22"
    id("com.google.devtools.ksp").version("1.9.22-1.0.17")
}

group = "edu.bridgew.comp490"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))

    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")
    ksp("dev.zacsweers.moshix:moshi-sealed-codegen:0.25.1")

    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("dev.zacsweers.moshix:moshi-sealed-runtime:0.25.1")

    implementation("com.github.serpapi:google-search-results-java:aff4a9caf3")

    implementation("javax.annotation:javax.annotation-api:1.3.2")

    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-RC2")

    testImplementation("com.squareup.okhttp3:mockwebserver")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.8.10")

}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

ksp {
    arg("moshi.generated", "javax.annotation.Generated")
}
