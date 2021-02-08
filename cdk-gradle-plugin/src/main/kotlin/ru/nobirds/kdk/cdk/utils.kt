package ru.nobirds.kdk.cdk

import com.moowork.gradle.node.npm.NpxExecRunner
import org.gradle.api.Project
import software.amazon.jsii.JsiiEngine
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.full.staticFunctions

fun Project.npx(workingDirectory: File, command: List<String>) {
    NpxExecRunner(project).apply {
        (arguments as MutableList<String>).addAll(command)
        ignoreExitValue = false
        workingDir = workingDirectory
    }.execute()
}

fun shutdownJsii() {
    JsiiEngine::class.shutdown()
}

fun KClass<JsiiEngine>.shutdown() {
    staticFunctions.firstOrNull { it.name == "reset" }?.call()
}
