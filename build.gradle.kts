@file:Suppress("LocalVariableName")

plugins {
    id("com.gradleup.shadow") version "9.2.2"
    id("com.gladed.androidgitversion")
    id("net.fabricmc.fabric-loom")
    `java-library`
}

androidGitVersion {
    format = "%tag%%+count%%.commit%%.dirty%"
    untrackedIsDirty = true
}

group = "arm32x.minecraft"
version = androidGitVersion.name()

configurations.implementation.get().extendsFrom(configurations["shadow"])

repositories {
    mavenCentral()
}

dependencies {
    val minecraft_version: String by project
    minecraft("com.mojang:minecraft:$minecraft_version")

    val loader_version: String by project
    implementation("net.fabricmc:fabric-loader:$loader_version")

    val fabric_api_version: String by project
    implementation("net.fabricmc.fabric-api:fabric-api:$fabric_api_version")

    val msgpack_java_version: String by project
    shadow("org.msgpack:msgpack-core:$msgpack_java_version")

    val junit_version: String by project
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit_version")

    val jqwik_version: String by project
    testImplementation("net.jqwik:jqwik:$jqwik_version")

    val assertj_version: String by project
    testImplementation("org.assertj:assertj-core:$assertj_version")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
    options.encoding = "UTF-8"
}

loom {
    accessWidenerPath.set(file("src/main/resources/commandblockide.accesswidener"))
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.jar {
    from("LICENSE")
}

tasks.shadowJar {
    configurations = listOf(project.configurations.shadow.get())
}

tasks.test {
    failOnNoDiscoveredTests = false
}
