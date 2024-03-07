
import de.undercouch.gradle.tasks.download.Download
import groovy.json.JsonSlurper

plugins {
    id("nmanos-jobs-project.data-import-conventions")

    alias(libs.plugins.sqldelight)
    alias(libs.plugins.download)
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(platform(libs.bom.log4j))

    ksp(libs.bundles.ksp.moshi)

    implementation(libs.bundles.koin)

    implementation(libs.kotlinspirit.core)

    implementation(libs.bundles.moshi)
    implementation(libs.retrofit.converter.moshi)

    implementation(libs.bundles.sqldelight)

    implementation(libs.prettytime.core)
    implementation(libs.bundles.log4j.impl)
    runtimeOnly(libs.bundles.log4j.runtime)

//    testImplementation(libs.test.koin)
    testImplementation(libs.test.okhttp.mockServer)
    testImplementation(libs.bundles.log4j.impl)
    testRuntimeOnly(libs.bundles.log4j.runtime)
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

    into(layout.projectDirectory.dir("test-data"))
}

tasks.test {
//    dependsOn(downloadTestSearchResults)
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
