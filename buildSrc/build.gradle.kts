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
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
    implementation("com.google.devtools.ksp:symbol-processing-gradle-plugin:1.9.22-1.0.17")
}
