plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    val retrofitVersion = "2.9.0"
    val coroutineVersion = "1.8.0"
    val mockkVersion = "1.13.9"

    implementation("app.cash.sqldelight:sqlite-driver:2.0.1")

    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")

    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")

    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:${kotlin.coreLibrariesVersion}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutineVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.2")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading")
    environment("JOBSPROJ_DEBUG_API" to "false", "JOBSPROJ_TEST_DIR" to layout.buildDirectory.dir("test-data").get().asFile.absolutePath)
}

kotlin {
    jvmToolchain(21)
}
