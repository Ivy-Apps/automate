plugins {
    kotlin("jvm") version libs.versions.kotlin
    kotlin("plugin.serialization") version libs.versions.kotlin
    alias(libs.plugins.anvil)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation(project(":openai-api"))
    implementation(project(":state-machine"))


    implementation(libs.bundles.arrow)
    implementation(libs.bundles.ktor)

    implementation(libs.dagger)
    implementation(libs.bundles.logging)

    testImplementation(libs.bundles.testing)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

anvil {
    generateDaggerFactories.set(true)
}