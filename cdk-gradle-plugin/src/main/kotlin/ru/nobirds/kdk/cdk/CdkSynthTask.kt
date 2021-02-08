package ru.nobirds.kdk.cdk

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property
import ru.nobirds.kdk.cdk.dsl.app
import software.amazon.awscdk.core.App
import software.amazon.awscdk.core.AppProps

typealias ApplicationConfigurer = App.() -> Unit

open class CdkSynthTask : DefaultTask() {

    @Input
    val environment = project.objects.mapProperty<String, String>()

    @Internal
    val application = project.objects.property<ApplicationConfigurer>()

    @Nested
    val dependencies = project.objects.listProperty<Any>()

    @OutputDirectory
    val outputDirectory = project.objects.directoryProperty()

    @TaskAction
    fun synth() {
        val props = AppProps.builder()
                .outdir(outputDirectory.get().asFile.absolutePath)
                .context(environment.get())
                .build()

        app(props, application.get()).synth()
    }

}
