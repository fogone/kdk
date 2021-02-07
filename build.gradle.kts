import pl.allegro.tech.build.axion.release.domain.TagNameSerializationConfig

plugins {
    id("pl.allegro.tech.build.axion-release") version "1.10.2"
}

scmVersion {
    tag(closureOf<TagNameSerializationConfig> {
        prefix = "release"
        versionSeparator = "/"
    })
}

val realVersion = scmVersion.version

allprojects {
    group = "ru.nobirds.kdk"
    version = realVersion

    repositories {
        jcenter()
        mavenCentral()
        maven("https://plugins.gradle.org/m2/")
    }
}

logger.quiet("Current version: ${scmVersion.version}")

