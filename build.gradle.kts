// Top-level build file. AGP 9 has built-in Kotlin, so we do NOT apply the
// `org.jetbrains.kotlin.android` plugin (it is incompatible with the AGP 9 DSL).
// We bump the Kotlin Gradle plugin to 2.4.21 (above AGP's bundled 2.2.10) so the
// Compose compiler matches the modern Compose BOM, per:
// https://developer.android.com/build/migrate-to-built-in-kotlin
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.21")
    }
}

plugins {
    id("com.android.application") version "9.4.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.21" apply false
}
