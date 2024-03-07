plugins {
    id("nmanos-jobs-project.kotlin-conventions")
}

dependencies {
    val poiVersion = "5.2.5"

    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))

    implementation("com.squareup.okhttp3:okhttp")

    implementation("org.apache.poi:poi:$poiVersion")
    implementation("org.apache.poi:poi-ooxml:$poiVersion")
    implementation("org.apache.commons:commons-compress:1.26.0")
}
