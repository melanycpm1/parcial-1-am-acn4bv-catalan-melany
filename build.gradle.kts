// build.gradle.kts (Project-level)
plugins {
    alias(libs.plugins.android.application) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Plugin Google Services
        classpath("com.google.gms:google-services:4.4.1")
        // Plugin Kotlin
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
    }
}