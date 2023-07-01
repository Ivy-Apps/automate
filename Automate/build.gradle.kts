import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.serialization") version "1.8.21"
    kotlin("kapt") version "1.8.21"
    //https://github.com/square/anvil
    id("com.squareup.anvil") version "2.4.6"

    application
}

group = "ivyapps.automate"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // region Arrow KT
    implementation(platform("io.arrow-kt:arrow-stack:1.2.0-RC"))
    implementation("io.arrow-kt:arrow-core")
    implementation("io.arrow-kt:arrow-fx-coroutines")
    // https://mvnrepository.com/artifact/io.arrow-kt/arrow-exact-jvm
    implementation("io.arrow-kt:arrow-exact-jvm:0.1.0")
    // endregion

    // region Ktor + Kotlinx serialization
    val ktorVersion = "2.3.2"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    // region Ktor Logging
    //https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
    implementation("ch.qos.logback:logback-classic:1.2.9")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    // endregion
    // endregion

    // region DI: Dagger + Anvil
    //https://github.com/google/dagger
    val daggerVersion = "2.46.1"
    implementation("com.google.dagger:dagger:$daggerVersion")
    kapt("com.google.dagger:dagger-compiler:$daggerVersion")
    // endregion

    // region Logging
    //https://github.com/oshai/kotlin-logging
    implementation("io.github.oshai:kotlin-logging-jvm:4.0.1")
    // https://mvnrepository.com/artifact/org.slf4j/slf4j-api
    implementation("org.slf4j:slf4j-log4j12:1.7.27")
    // https://mvnrepository.com/artifact/org.slf4j/slf4j-reload4j
    implementation("org.slf4j:slf4j-reload4j:2.0.7")
    // endregion

    // region Kotest
    val kotestVersion = "5.6.2"
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
    //https://kotest.io/docs/extensions/ktor.html
    testImplementation("io.kotest.extensions:kotest-assertions-ktor:2.0.0")
    //https://kotest.io/docs/assertions/arrow.html
    testImplementation("io.kotest.extensions:kotest-assertions-arrow:1.3.3")
    // endregion

    testImplementation(kotlin("test"))
}

tasks.withType<JavaExec> {
    systemProperty("OPEN_AI_API_KEY", project.findProperty("ivyapps_openai_api_key") as String? ?: "")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}