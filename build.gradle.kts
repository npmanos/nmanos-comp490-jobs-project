import de.undercouch.gradle.tasks.download.Download
import groovy.json.JsonSlurper

plugins {
    kotlin("jvm") version "1.9.22"
    application
    id("app.cash.sqldelight") version "2.0.1"
    id("com.google.devtools.ksp") version "1.9.22-1.0.17"
    id("com.jaredsburrows.license") version "0.9.7"
    id("de.undercouch.download").version("5.5.0")
}

group = "edu.bridgew.comp490"
version = "3.0.0-SNAPSHOT"

application {
    mainClass = "edu.bridgew.comp490.proj1.MainKt"
    applicationName = "job-search"
}

distributions {
    main {
        contents {
            from("README.md", "sample.env", "NOTICE.html")
        }
    }
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    val retrofitVersion = "2.9.0"
    val moshiSealedVersion = "0.25.1"
    val moshiVersion = "1.15.1"
    val sqlDelightVersion = "2.0.1"
    val prettytimeVersion = "5.0.7.Final"
    val slf4jVersion = "1.7.36"
    val poiVersion = "5.2.5"
    val mockkVersion = "1.13.9"

    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    implementation(platform("org.apache.logging.log4j:log4j-bom:2.22.1"))

    ksp("com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion")
    ksp("dev.zacsweers.moshix:moshi-sealed-codegen:$moshiSealedVersion")

    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.moshi:moshi:$moshiVersion")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-moshi:$retrofitVersion")
    implementation("dev.zacsweers.moshix:moshi-sealed-runtime:$moshiSealedVersion")

    implementation("app.cash.sqldelight:sqlite-driver:$sqlDelightVersion")
    implementation("app.cash.sqldelight:primitive-adapters:$sqlDelightVersion")
    implementation("app.cash.sqldelight:coroutines-extensions:$sqlDelightVersion")

    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    implementation("org.ocpsoft.prettytime:prettytime:$prettytimeVersion")
//    implementation("org.slf4j:slf4j-nop:$slf4jVersion")
    implementation("org.apache.logging.log4j:log4j-api")
    runtimeOnly("org.apache.logging.log4j:log4j-core")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl")

    implementation("com.github.ajalt.clikt:clikt:4.2.2")

    implementation("org.apache.poi:poi:$poiVersion")
    implementation("org.apache.poi:poi-ooxml:$poiVersion")

    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("com.squareup.okhttp3:mockwebserver")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:${kotlin.coreLibrariesVersion}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0-RC2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.2")
//    testImplementation("org.slf4j:slf4j-nop:$slf4jVersion")
    testImplementation("org.apache.logging.log4j:log4j-api")
    testRuntimeOnly("org.apache.logging.log4j:log4j-core")
    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl")
}

val downloadTestSearchResults by tasks.register<Download>("downloadTestSearchResults") {
    val searchResultsUrls = file("src/main/resources/edu/bridgew/comp490/proj1/data/debug/search_result_urls.txt")
    inputs.file(searchResultsUrls)
        .withPropertyName("searchResultUrlsFile")
        .skipWhenEmpty()

    val outputDir = layout.buildDirectory.dir("test-data/raw")

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

    into(layout.buildDirectory.dir("test-data"))
}

sqldelight {
    databases {
        create("JobSearchDB") {
            packageName.set("edu.bridgew.comp490.proj1.data.db")
            schemaOutputDirectory.set(file("src/main/sqldelight/databases"))
            dialect("app.cash.sqldelight:sqlite-3-38-dialect:2.0.1")
        }
    }
}

licenseReport {
    generateCsvReport = false
    generateHtmlReport = true
    generateJsonReport = false
    generateTextReport = false
}

val copyLicenseNotice by tasks.register<Copy>("copyLicenseNotice") {
    tasks["generateMainJobSearchDBInterface"].dependsOn(this) // I know not why this is true, but it is
    dependsOn(tasks["licenseReport"])
    from(tasks["licenseReport"])
    rename { "NOTICE${it.removePrefix("licenseReport")}" }
    into(layout.projectDirectory)
}

tasks.test {
    dependsOn(downloadTestSearchResults)
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading")
    environment("JOBSPROJ_DEBUG_API" to "false", "JOBSPROJ_TEST_DIR" to layout.buildDirectory.dir("test-data").get().asFile.absolutePath)
}

val copyDist by tasks.register<Copy>("copyDist") {
    from(tasks.installDist)
    into(layout.projectDirectory.dir("dist"))
}

tasks.installDist {
    dependsOn(copyLicenseNotice)
    finalizedBy(copyDist)
}

tasks.distZip {
    archiveVersion = "ghbuild"
}

kotlin {
    jvmToolchain(21)
}

ksp {
    arg("moshi.generated", "javax.annotation.processing.Generated")
}
