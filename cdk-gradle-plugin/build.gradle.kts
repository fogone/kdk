import com.gradle.publish.PluginConfig
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.12.0"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.github.node-gradle.node:com.github.node-gradle.node.gradle.plugin:2.2.3")
    api("software.amazon.awscdk:core:1.75.0")
    api(project(":cdk-dsl"))
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

gradlePlugin {
}

pluginBundle {
    website = "https://github.com/fogone/kdk"
    vcsUrl = "https://github.com/fogone/kdk"

    plugins {
        findOrCreate("ru.nobirds.kdk.cdk") {
            displayName = "Kotlin Cdk Gradle Plugin"
            description = "This plugin allows to define cdk config and run it from gradle"
            tags = listOf("kotlin", "aws", "cdk", "k8s", "deployment")
        }
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}

fun NamedDomainObjectContainer<PluginConfig>.findOrCreate(
    name: String, configuration: PluginConfig.() -> Unit): PluginConfig {

    val config = findByName(name) ?: create(name)

    return config.apply(configuration)
}
