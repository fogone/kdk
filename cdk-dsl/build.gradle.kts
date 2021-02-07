plugins {
    kotlin("jvm") version "1.4.30"
    `central-publishing`
    id("cdk-generator")
}

repositories {
    mavenCentral()
}

tasks {
    compileKotlin.configure {
        kotlinOptions.jvmTarget = "1.8"
        dependsOn(generateCdkModel)
    }
}
