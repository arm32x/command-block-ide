plugins {
    alias(libs.plugins.android.git.version)
    alias(libs.plugins.fabric.loom)
    `java-library`
}

androidGitVersion {
    format = "%tag%%+count%%.commit%%.dirty%"
    untrackedIsDirty = true
}

group = "arm32x.minecraft"
version = androidGitVersion.name()

repositories {
    mavenCentral()
}

dependencies {
    minecraft(libs.minecraft)

    implementation(libs.fabric.loader)
    implementation(libs.fabric.api)

    implementation(libs.msgpack.core)
    include(libs.msgpack.core)

    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.jqwik)
    testImplementation(libs.assertj.core)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt()))
    }
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(libs.versions.java.get().toInt())
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
    from("LICENSE") {
        rename { "${it}_${project.name}" }
    }
}

tasks.test {
    useJUnitPlatform()
}
