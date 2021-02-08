package ru.nobirds.kdk.cdk

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.api.tasks.options.Option
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property

open class CdkDeployTask : DefaultTask() {

    @Input
    @Optional
    val awsProfile = project.objects.property<String>()

    @Input
    val requiredApproval = project.objects.property<Boolean>().convention(false)

    @Input
    val stacks = project.objects.listProperty<String>()

    @InputDirectory
    val applicationDirectory = project.objects.directoryProperty()

    @Internal
    val outputsFile = project.objects.fileProperty()

    @Option(option = "stacks", description = "Defines stacks to process")
    fun defineStacks(stacks: List<String>) {
        this.stacks.addAll(stacks)
    }

    @TaskAction
    fun deploy() {
        val command = mutableListOf("cdk", "deploy",
            "--app", applicationDirectory.get().asFile.absolutePath,
            "--outputs-file", outputsFile.get().asFile.absolutePath
        )

        if(!requiredApproval.get()) {
            command.addAll(listOf("--require-approval", "never"))
        }

        if (awsProfile.isPresent) {
            command.addAll(listOf("--profile", awsProfile.get()))
        }

        if (stacks.get().isNotEmpty()) {
            command.addAll(stacks.get())
        } else {
            command.add("--all")
        }

        project.npx(project.projectDir, command)
    }

}

