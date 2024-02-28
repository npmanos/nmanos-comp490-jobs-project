plugins {
    id("nmanos-jobs-project.data-import-conventions")

    application
}

application {
    mainClass = "edu.bridgew.comp490.proj1.MainKt"
    applicationName = "job-search"
}

distributions {
    main {
        contents {
            from("README.md", "sample.env", "NOTICE.html")
            from(layout.projectDirectory) {
                include("data/**")
                exclude("data/**/~$*.xls", "data/**/~$*.xlsx")
            }
        }
    }
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(project(":shared"))

    implementation(libs.clikt.core)
}

val copyDist by tasks.register<Copy>("copyDist") {
    from(tasks.installDist)
    into(layout.projectDirectory.dir("dist"))
}

tasks.installDist {
    dependsOn(":copyLicenseNotice")
    finalizedBy(copyDist)
}

tasks.distZip {
    archiveVersion = "ghbuild"
}

ksp {
    arg("moshi.generated", "javax.annotation.processing.Generated")
}
