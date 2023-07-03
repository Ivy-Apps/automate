plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.anvil)
}

repositories {
    mavenCentral()
}

dependencies {
    // https://github.com/gradle/gradle/issues/15383
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
//    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
}