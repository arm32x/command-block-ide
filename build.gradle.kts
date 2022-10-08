@file:Suppress("LocalVariableName")

plugins {
    id("com.github.johnrengelman.shadow")
    id("com.gladed.androidgitversion")
    id("fabric-loom")
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
    val yarn_mappings: String by project
    mappings("net.fabricmc:yarn:$yarn_mappings:v2")
    val loader_version: String by project
    modImplementation("net.fabricmc:fabric-loader:$loader_version")

    val fabric_version: String by project
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabric_version")

    val msgpack_java_version: String by project
    shadow("org.msgpack:msgpack-core:${msgpack_java_version}")

    val junit_version: String by project
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junit_version}")
    val jqwik_version: String by project
    testImplementation("net.jqwik:jqwik:${jqwik_version}")
    val assertj_version: String by project
    testImplementation("org.assertj:assertj-core:${assertj_version}")

    val layout_version: String by project
    shadow("io.github.abvadabra:layout-java:$layout_version")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

loom {
    accessWidenerPath.set(File("src/main/resources/commandblockide.accesswidener"))
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

// Reproducible builds (or at least an attempt)
tasks.withType<AbstractArchiveTask> {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.jar {
    from("LICENSE")
}

tasks.shadowJar {
    configurations = listOf(project.configurations.shadow.get())
}

tasks.remapJar {
    dependsOn(tasks.shadowJar)
    inputFile.set(tasks.shadowJar.get().archiveFile)
    doLast {
        tasks.shadowJar.get().archiveFile.get().asFile.delete()
    }
}
