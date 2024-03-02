plugins {
    kotlin("jvm")
    id("com.jaredsburrows.license") version "0.9.7"
}

repositories {
    mavenCentral()
}

licenseReport {
    generateCsvReport = false
    generateHtmlReport = true
    generateJsonReport = false
    generateTextReport = true
}

val copyLicenseNotice by tasks.register<Copy>("copyLicenseNotice") {
    dependsOn(tasks["licenseReport"])
    from(tasks["licenseReport"])
    rename { "NOTICE${it.removePrefix("licenseReport")}" }
    into(layout.projectDirectory)
}
