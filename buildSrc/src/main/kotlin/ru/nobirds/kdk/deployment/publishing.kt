package ru.nobirds.kdk.deployment

import org.gradle.api.publish.PublishingExtension
import java.net.URI

fun PublishingExtension.defaultPublishingRepository(version: Any) {
    repositories {
        maven {
            name = "sonatype"
            val repoName = if(version.toString().endsWith("SNAPSHOT")) "snapshots" else "releases"

            url = URI("https://oss.sonatype.org/service/local/staging/deploy/maven2/")

            credentials {
                username = System.getProperty("SONATYPE_USER")
                password = System.getProperty("SONATYPE_PASSWORD")
            }
        }
    }
}
