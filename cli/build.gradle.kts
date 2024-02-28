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

    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    implementation(platform("org.apache.logging.log4j:log4j-bom:2.23.0"))

    implementation("com.squareup.okhttp3:okhttp")

    implementation("com.github.ajalt.clikt:clikt:4.2.2")
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
