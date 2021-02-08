package ru.nobirds.kdk.cdk

import org.gradle.api.Project
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property
import software.amazon.awscdk.core.App

open class CdkExtension(project: Project) {

    val awsProfile = project.objects.property<String>()

    val requiredApproval = project.objects.property<Boolean>().convention(false)

    val outputDirectory = project.objects.directoryProperty()
            .convention(project.layout.buildDirectory.dir("cdk"))

    val application = project.objects.property<ApplicationConfigurer>()

    val environment = project.objects.mapProperty<String, String>()

    val dependencies = project.objects.listProperty<Any>()

}

fun CdkExtension.requiredApproval() {
    this.requiredApproval.set(true)
}

fun CdkExtension.awsProfile(profile: String) {
    this.awsProfile.set(profile)
}

fun CdkExtension.application(app: App.() -> Unit) {
    this.application.set(app)
}

fun CdkExtension.dependency(vararg dependency: Any) {
    this.dependencies.addAll(*dependency)
}

fun CdkExtension.env(key: String, value: String) {
    this.environment.put(key, value)
}
