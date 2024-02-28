plugins {
    `kotlin-dsl`
}

group = "edu.bridgew.comp490"
version = "4.0.0-SNAPSHOT"

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.buildSrc.plugins)
}
