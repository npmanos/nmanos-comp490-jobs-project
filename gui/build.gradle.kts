
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

//    val retrofitVersion = "2.9.0"
//    val moshiSealedVersion = "0.25.1"
//    val moshiVersion = "1.15.1"
//    val sqlDelightVersion = "2.0.1"
    val coroutineVersion = "1.8.0"
//    val prettytimeVersion = "5.0.7.Final"
//    val poiVersion = "5.2.5"
    val voyagerVersion = "1.0.0"
//    val mockkVersion = "1.13.9"

//    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
//    implementation(platform("org.apache.logging.log4j:log4j-bom:2.23.0"))
//
//    ksp("com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion")
//    ksp("dev.zacsweers.moshix:moshi-sealed-codegen:$moshiSealedVersion")
//
//    implementation("com.squareup.okhttp3:okhttp")
//    implementation("com.squareup.moshi:moshi:$moshiVersion")
//    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
//    implementation("com.squareup.retrofit2:converter-moshi:$retrofitVersion")
//    implementation("dev.zacsweers.moshix:moshi-sealed-runtime:$moshiSealedVersion")
//
//    implementation("app.cash.sqldelight:sqlite-driver:$sqlDelightVersion")
//    implementation("app.cash.sqldelight:primitive-adapters:$sqlDelightVersion")
//    implementation("app.cash.sqldelight:coroutines-extensions:$sqlDelightVersion")


    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:$coroutineVersion") // Needed by voyager for coroutine integration

//    implementation("org.ocpsoft.prettytime:prettytime:$prettytimeVersion")
//    implementation("org.apache.logging.log4j:log4j-api")
//    runtimeOnly("org.apache.logging.log4j:log4j-core")
//    implementation("org.apache.logging.log4j:log4j-slf4j-impl")

//    implementation("com.github.ajalt.clikt:clikt:4.2.2")

//    implementation("org.apache.poi:poi:$poiVersion")
//    implementation("org.apache.poi:poi-ooxml:$poiVersion")
//    implementation("org.apache.commons:commons-compress:1.26.0")

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

//    testImplementation("com.squareup.okhttp3:mockwebserver")

//    testImplementation("org.apache.logging.log4j:log4j-api")
//    testRuntimeOnly("org.apache.logging.log4j:log4j-core")
//    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl")
}
