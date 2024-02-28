
import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    id("nmanos-jobs-project.kotlin-conventions")

    id("org.jetbrains.compose") version "1.5.12"
}

compose.desktop {
    application {
        mainClass = "edu.bridgew.comp490.proj1.ui.MainKt"
    }
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

@OptIn(ExperimentalComposeLibrary::class)
dependencies {
    implementation(project(":shared"))

    val coroutineVersion = "1.8.0"
    val voyagerVersion = "1.0.0"


    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:$coroutineVersion") // Needed by voyager for coroutine integration

    implementation(compose.components.resources)
    implementation(compose.desktop.components.splitPane)
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation("cafe.adriel.voyager:voyager-navigator:$voyagerVersion")
    implementation("cafe.adriel.voyager:voyager-screenmodel:$voyagerVersion")
    implementation("cafe.adriel.voyager:voyager-bottom-sheet-navigator:$voyagerVersion")
    implementation("cafe.adriel.voyager:voyager-tab-navigator:$voyagerVersion")
    implementation("cafe.adriel.voyager:voyager-transitions:$voyagerVersion")
}
