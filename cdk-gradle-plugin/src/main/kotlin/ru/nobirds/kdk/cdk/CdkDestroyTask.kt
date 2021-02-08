package ru.nobirds.kdk.cdk

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property

open class CdkDestroyTask : DefaultTask() {

    @Input
    @Optional
    val awsProfile = project.objects.property<String>()

    @Input
    val requiredApproval = project.objects.property<Boolean>().convention(false)

    @Input
    val stacks = project.objects.listProperty<String>()

    @InputDirectory
    val applicationDirectory = project.objects.directoryProperty()

    @Option(option = "stacks", description = "Defines stacks to process")
    fun defineStacks(stacks: List<String>) {
        this.stacks.addAll(stacks)
    }

    @TaskAction
    fun deploy() {
        val command = mutableListOf("cdk", "destroy",
                "--app", applicationDirectory.get().asFile.absolutePath)

        if(!requiredApproval.get()) {
            command.add("--force")
        }

        if (awsProfile.isPresent) {
            command.addAll(listOf("--profile", awsProfile.get()))
        }

        if (stacks.get().isNotEmpty()) {
            command.addAll(stacks.get())
        }

        project.npx(project.projectDir, command)
    }

}
