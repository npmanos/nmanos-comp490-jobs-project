
import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    id("nmanos-jobs-project.kotlin-conventions")

    alias(libs.plugins.compose)
}

compose.desktop {
    application {
        mainClass = "edu.bridgew.comp490.proj1.ui.MainKt"
        nativeDistributions {
            modules("java.instrument", "java.management", "java.security.jgss", "java.sql", "java.xml.crypto", "jdk.unsupported")
        }
    }
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    google()
}

@OptIn(ExperimentalComposeLibrary::class)
dependencies {
    implementation(project(":shared"))

    implementation(compose.components.resources)
    implementation(compose.desktop.components.splitPane)
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(libs.composeFilePicker)
    implementation(libs.constraintLayoutKMP)

    implementation(libs.commons.lang)

    implementation(libs.bundles.koin)
    implementation(libs.bundles.landscapist)
    implementation(libs.bundles.voyager)

//    testImplementation(libs.test.koin)
}
