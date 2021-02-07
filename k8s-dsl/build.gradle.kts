plugins {
    kotlin("jvm") version "1.4.30"
    `central-publishing`
    id("k8s-generator")
}

repositories {
    mavenCentral()
}

dependencies {
    api("io.fabric8:kubernetes-client:5.0.1")
}

tasks {
    compileKotlin.configure {
        dependsOn(generateK8sModel)
    }
}
