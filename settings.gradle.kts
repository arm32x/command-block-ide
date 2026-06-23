@file:Suppress("LocalVariableName")

pluginManagement {
    repositories {
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        gradlePluginPortal()
    }
    plugins {
        val android_git_version_version: String by settings
        val loom_version: String by settings
        val shadow_version: String by settings

        id("com.gradleup.shadow") version shadow_version
        id("com.gladed.androidgitversion") version android_git_version_version
        id("net.fabricmc.fabric-loom") version loom_version
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "command-block-ide"
