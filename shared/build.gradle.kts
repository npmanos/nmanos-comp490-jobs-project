
import de.undercouch.gradle.tasks.download.Download
import groovy.json.JsonSlurper

plugins {
    id("nmanos-jobs-project.data-import-conventions")

    id("app.cash.sqldelight") version "2.0.1"
    id("de.undercouch.download").version("5.5.0")
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
    val coroutineVersion = "1.8.0"
    val prettytimeVersion = "5.0.7.Final"

//    val mockkVersion = "1.13.9"

    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    implementation(platform("org.apache.logging.log4j:log4j-bom:2.23.0"))

    ksp("com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion")
    ksp("dev.zacsweers.moshix:moshi-sealed-codegen:$moshiSealedVersion")

    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.moshi:moshi:$moshiVersion")
    implementation("com.squareup.retrofit2:converter-moshi:$retrofitVersion")
    implementation("dev.zacsweers.moshix:moshi-sealed-runtime:$moshiSealedVersion")

    implementation("app.cash.sqldelight:primitive-adapters:$sqlDelightVersion")
    implementation("app.cash.sqldelight:coroutines-extensions:$sqlDelightVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")

    implementation("org.ocpsoft.prettytime:prettytime:$prettytimeVersion")
    implementation("org.apache.logging.log4j:log4j-api")
    runtimeOnly("org.apache.logging.log4j:log4j-core")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl")

    testImplementation("com.squareup.okhttp3:mockwebserver")
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

tasks.test {
    dependsOn(downloadTestSearchResults)
}

sqldelight {
    databases {
        create("JobSearchDB") {
            packageName.set("edu.bridgew.comp490.proj1.data.db")
            schemaOutputDirectory.set(layout.projectDirectory.dir("src/main/sqldelight/databases"))
            dialect("app.cash.sqldelight:sqlite-3-38-dialect:2.0.1")
        }
    }
}

ksp {
    arg("moshi.generated", "javax.annotation.processing.Generated")
}
