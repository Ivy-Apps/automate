import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.21"
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

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}