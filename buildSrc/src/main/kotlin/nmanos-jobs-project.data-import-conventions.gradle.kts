plugins {
    id("nmanos-jobs-project.kotlin-conventions")
}

dependencies {
    val poiVersion = "5.2.5"

    implementation("org.apache.poi:poi:$poiVersion")
    implementation("org.apache.poi:poi-ooxml:$poiVersion")
    implementation("org.apache.commons:commons-compress:1.26.0")
}
