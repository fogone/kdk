import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.`maven-publish`
import org.gradle.kotlin.dsl.get
import ru.nobirds.kdk.deployment.defaultPublishingRepository

plugins {
    `maven-publish`
    // signing
}

publishing {
    defaultPublishingRepository(project.version)

    publications {
        publications {
            register<MavenPublication>("mavenJava") {
                from(components["java"])

                groupId = project.group.toString()
                artifactId = project.name
                version = project.version.toString()

                pom {
                    name.set(project.name)
                    description.set("Kotlin Deployment DSL-s")
                    url.set("https://github.com/fogone/kdk")

                    licenses {
                        license {
                            name.set("MIT License")
                        }
                    }

                    developers {
                        developer {
                            id.set("fogone")
                            name.set("Boris Vanin")
                            email.set("fogone@gmail.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:github.com/fogone/kdk.gti")
                        developerConnection.set("scm:git:ssh://github.com/fogone/kdk.gti")
                        url.set("https://github.com/fogone/kdk")
                    }
                }
            }
        }
    }
}
