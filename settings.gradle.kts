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

        id("com.github.johnrengelman.shadow") version shadow_version
        id("com.gladed.androidgitversion") version android_git_version_version
        id("fabric-loom") version loom_version
    }
}

rootProject.name = "command-block-ide"
