import ru.nobirds.kdk.deployment.defaultPublishingRepository

plugins {
    kotlin("jvm") version "1.3.72"
    `maven-publish`
    id("cdk-generator")
}

repositories {
    mavenCentral()
}

publishing {
    defaultPublishingRepository(project.version)

    publications {
        publications {
            register<MavenPublication>("mavenJava") {
                from(components["java"])
            }
        }
    }
}

tasks {
    compileKotlin.configure {
        kotlinOptions.jvmTarget = "1.8"
        dependsOn(generateCdkModel)
    }
}
