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
//    tasks["generateMainJobSearchDBInterface"].dependsOn(this) // I know not why this is true, but it is
    dependsOn(tasks["licenseReport"])
    from(tasks["licenseReport"])
    rename { "NOTICE${it.removePrefix("licenseReport")}" }
    into(layout.projectDirectory)
}
