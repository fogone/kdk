package ru.nobirds.kdk

import ru.nobirds.kdk.cdk.CdkDeployTask
import ru.nobirds.kdk.cdk.CdkDestroyTask
import ru.nobirds.kdk.cdk.CdkExtension
import ru.nobirds.kdk.cdk.CdkSynthTask

plugins {
    id("com.github.node-gradle.node")
}

node {
    version = "15.8.0"
    yarnVersion = "1.22.10"
    download = true
}

val extension = extensions.create<CdkExtension>("cdk", project)

tasks {
    val cdkSynth by registering(CdkSynthTask::class) {
        group = "deployment"

        environment.set(extension.environment)
        outputDirectory.set(extension.outputDirectory.dir("synth"))
        application.set(extension.application)

        dependencies.set(extension.dependencies)

        dependsOn(extension.dependencies)
    }

    val cdkDeploy by registering(CdkDeployTask::class) {
        group = "deployment"

        awsProfile.set(extension.awsProfile)
        requiredApproval.set(extension.requiredApproval)
        applicationDirectory.set(cdkSynth.flatMap { it.outputDirectory })

        outputsFile.set(extension.outputDirectory.file("outputs/outputs.json"))

        dependsOn(yarnSetup, cdkSynth)
    }
    val cdkDestroy by registering(CdkDestroyTask::class) {
        group = "deployment"

        awsProfile.set(extension.awsProfile)
        requiredApproval.set(extension.requiredApproval)
        applicationDirectory.set(cdkSynth.flatMap { it.outputDirectory })

        dependsOn(yarnSetup, cdkSynth)
    }
}
