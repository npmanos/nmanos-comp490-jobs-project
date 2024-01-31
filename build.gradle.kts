import de.undercouch.gradle.tasks.download.Download
import groovy.json.JsonSlurper

plugins {
    kotlin("jvm") version "1.9.22"
    id("com.google.devtools.ksp").version("1.9.22-1.0.17")
    id("de.undercouch.download").version("5.5.0")
}

group = "edu.bridgew.comp490"
version = "1.0-SNAPSHOT"

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
    implementation("org.slf4j:slf4j-nop:$slf4jVersion")

    testImplementation("com.squareup.okhttp3:mockwebserver")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:${kotlin.coreLibrariesVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.1")
    testImplementation("org.slf4j:slf4j-nop:$slf4jVersion")
}

val downloadTestSearchResults by tasks.register<Download>("downloadTestSearchResults") {
    val searchResultsUrls = file("src/main/resources/edu/bridgew/comp430/proj1/api/debug/search_result_urls.txt")
    inputs.file(searchResultsUrls)
        .withPropertyName("searchResultUrlsFile")
        .skipWhenEmpty()

    val outputDir = layout.projectDirectory.dir("src/test/resources/edu/bridgew/comp430/proj1/api/responses/raw")

    src(resources.text.fromFile(searchResultsUrls).asReader().readLines())
    dest(outputDir)
    tempAndMove(true)
    overwrite(false)
    eachFile {
        val baseName = sourceURL.path.split('/').removeLast()
        name = baseName
    }

    finalizedBy(tasks["renameTestSearchResults"])
}

val renameTestSearchResults by tasks.register<Copy>("renameTestSearchResults") {
    val inputFiles = files(downloadTestSearchResults.outputFiles)
    from(inputFiles)

    val jsonSlurper = JsonSlurper()
    rename { oldName ->
        val file = inputFiles.single { it.name.contains(oldName) }
        val json: Map<String, Map<String, Any>> = jsonSlurper.parse(file) as Map<String, Map<String, Any>>
        val searchQ = (json["search_parameters"]!!["q"]!! as String).replace(' ', '_')
        val createdAt = (json["search_metadata"]!!["created_at"]!! as String).split(' ')
        val start = json["search_parameters"]!!["start"] as Int? ?: 0
        "$searchQ-${createdAt[0].filterNot { it == '-' }}${createdAt[1].filterNot { it == ':' }}-$start.json"
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    into("src/test/resources/edu/bridgew/comp430/proj1/api/responses")
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
