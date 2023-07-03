import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version libs.versions.kotlin
    kotlin("plugin.serialization") version libs.versions.kotlin
    kotlin("kapt") version libs.versions.kotlin
    alias(libs.plugins.anvil)

    application
}

group = "ivyapps.automate"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.arrow)
    implementation(libs.bundles.ktor)

    implementation(libs.dagger)
    kapt(libs.dagger.compiler)

    implementation(libs.bundles.logging)

    testImplementation(libs.bundles.kotest)
    testImplementation(kotlin("test"))
}

tasks.withType<JavaExec> {
    systemProperty(
        "OPEN_AI_API_KEY",
        project.property("ivyapps_openai_api_key") as String? ?: ""
    )
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("automate.MainKt")
}