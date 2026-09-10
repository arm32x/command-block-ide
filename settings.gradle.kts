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

        id("com.gradleup.shadow") version "9.2.2"
        id("com.gladed.androidgitversion") version android_git_version_version
        id("net.fabricmc.fabric-loom") version loom_version
    }
}

rootProject.name = "command-block-ide"
