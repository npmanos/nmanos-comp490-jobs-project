plugins {
    kotlin("jvm") version "1.9.22"
    id("com.google.devtools.ksp").version("1.9.22-1.0.17")
}

group = "edu.bridgew.comp490"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    val retrofitVersion = "2.9.0"
    val moshiSealedVersion = "0.25.1"
    val prettytimeVersion = "5.0.7.Final"
    val slf4jVersion = "1.7.36"
    val mockkVersion = "1.13.9"

    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))

    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")
    ksp("dev.zacsweers.moshix:moshi-sealed-codegen:$moshiSealedVersion")

    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-moshi:$retrofitVersion")
    implementation("dev.zacsweers.moshix:moshi-sealed-runtime:$moshiSealedVersion")

    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-RC2")

    implementation("org.ocpsoft.prettytime:prettytime:$prettytimeVersion")
//    implementation("org.ocpsoft.prettytime:prettytime-nlp:$prettytimeVersion")
    implementation("org.slf4j:slf4j-nop:$slf4jVersion")

    testImplementation("com.squareup.okhttp3:mockwebserver")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.8.10")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.1")
    testImplementation("org.slf4j:slf4j-nop:$slf4jVersion")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading")
}

kotlin {
    jvmToolchain(21)
}

ksp {
    arg("moshi.generated", "javax.annotation.processing.Generated")
}
