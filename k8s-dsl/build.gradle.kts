import ru.nobirds.kdk.deployment.defaultPublishingRepository

plugins {
    kotlin("jvm") version "1.3.72"
    `maven-publish`
    id("k8s-generator")
}

repositories {
    mavenCentral()
}

dependencies {
    api("io.fabric8:kubernetes-client:4.9.1")
    implementation(kotlin("stdlib"))
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
        dependsOn(generateK8sModel)
    }
}
