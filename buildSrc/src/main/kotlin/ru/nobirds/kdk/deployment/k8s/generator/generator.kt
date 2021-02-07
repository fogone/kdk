package ru.nobirds.kdk.deployment.k8s.generator

import io.fabric8.kubernetes.api.builder.Fluent
import io.fabric8.kubernetes.api.builder.Nested
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.reflections.ReflectionUtils
import org.reflections.Reflections
import org.reflections.scanners.MethodParameterScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import java.io.OutputStreamWriter
import java.lang.reflect.*
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.reflect.full.isSubclassOf
import ru.nobirds.kdk.deployment.*

open class K8sModelGeneratorTask @Inject constructor(private val objects: ObjectFactory) : DefaultTask() {

    @OutputDirectory
    val outputDirectory = objects.directoryProperty()

    private data class MethodProperty(val name: String, val getter: Method, val setter: Method)
    private data class Methods(val buildingMethods: List<Method>, val propertyMethods: List<MethodProperty>)

    @TaskAction
    fun generate() {
        val executorService = Executors.newFixedThreadPool(8)

        val reflections = Reflections(ConfigurationBuilder()
                .addUrls(ClasspathHelper.forPackage("io.fabric8.kubernetes.api.model"))
                .addScanners(MethodParameterScanner(), SubTypesScanner())
                .setExecutorService(executorService)
        )

        val fluents = reflections.getSubTypesOf(Fluent::class.java)
                .filter { it.isInterface && it.interfaces.any { it == Fluent::class.java } && it.name.endsWith("Fluent") }

        val methods = fluents.map {
            it to it.findMethods()
        }.toMap()

        val output = outputDirectory.asFile.get()
        output.mkdirs()

        for ((type, methodList) in methods) {
            output.resolve("${type.simpleName}.kt").outputStream().writer().use {
                it.appendSourceHeader()

                for (method in methodList.buildingMethods.sortedBy { it.name }) {
                    it.appendln(generateBuilderMethod(method))
                }

                for (property in methodList.propertyMethods) {
                    it.appendln(generateProperty(property))
                }
            }
        }

    }

    private fun OutputStreamWriter.appendSourceHeader() {
        appendln("package ru.nobirds.kdk.k8s.fabric.dsl")
        appendln()
        appendln("import io.fabric8.kubernetes.api.builder.*")
        appendln("import java.util.*")
        appendln()
    }

    private data class MethodParameter(val parameter: Parameter, val genericType: Type) {

        fun generateCall(): String = parameter.name

        fun generateDeclaration(): String = buildString {
            append("${parameter.name}: ${genericType.typeName.translate()}")
        }

    }

    private fun Method.parameters(): List<MethodParameter> =
            parameters.zip(genericParameterTypes).map {
                MethodParameter(it.first, it.second)
            }

    private fun generateBuilderMethod(method: Method): String = buildString {
        val type = method.declaringClass

        val builderTypeName = method.returnType.canonicalName
        val methodName = generateBuilderMethodName(method.name)
        val builderParameter = "builder: @K8sDsl ${builderTypeName}<T>.() -> Unit"
        val parameters = method.parameters()

        val allParameters = parameters.map { it.generateDeclaration() } + builderParameter

        appendln("""@K8sDsl inline fun <T: ${type.canonicalName}<T>> T.`$methodName`(${allParameters.joinToString(", ")}) {""")
        appendln("""    ${method.name}(${parameters.joinToString(", ") { it.generateCall() } }).apply(builder).and()""")
        appendln("""}""")
    }

    private fun generateBuilderMethodName(name: String): String = when {
        name.startsWith("withNew") -> name.replace("withNew", "").firstToLowerCase()
        name.startsWith("addNew") -> name.replace("addNew", "").firstToLowerCase()
        else -> name
    }

    private fun generateProperty(property: MethodProperty): String = buildString {
        val type = property.getter.declaringClass
        val propertyType = property.getter.genericReturnType.typeName.translate()

        appendln("""@K8sDsl inline var <T: ${type.canonicalName}<T>> T.`${property.name}Value`: $propertyType """)
        appendln("""    get() = ${property.getter.name}()""")
        appendln("""    set(value) { ${property.setter.name}(value) }""")
    }

    private fun Method.isBuilderMethod(): Boolean = returnType.kotlin.isSubclassOf(Nested::class)

    private fun <T> Class<T>.findMethods(): Methods {
        val builderMethods = ReflectionUtils.getAllMethods(this).filter { it.isBuilderMethod() }

        val allMethods = ReflectionUtils.getAllMethods(this).groupBy { it.name }
        val getterMethods = allMethods.filterKeys { it.startsWith("build") || it.startsWith("get") }
                .flatMap { it.value }
                .filter { it.parameters.isEmpty() && it.getDeclaredAnnotation(java.lang.Deprecated::class.java) == null }

        val properties = getterMethods
                .map { it.findPropertyName() to it }
                .mapNotNull { (name, getter) ->
            allMethods["with${name.firstToUpperCase()}"]
                    ?.firstOrNull { it.parameters.size == 1 && it.parameters.first().type == getter.returnType }
                    ?.let { MethodProperty(name, getter, it) }
        }

        val filteredProperties = properties.groupBy { it.name }
                .mapValues { it.value.firstOrNull { it.getter.name.startsWith("get") } ?: it.value.first() }
                .values.toList()

        return Methods(builderMethods, filteredProperties)
    }

    private fun Method.findPropertyName(): String = when {
        name.startsWith("build") -> name.substringAfter("build").firstToLowerCase()
        name.startsWith("get") -> name.substringAfter("get").firstToLowerCase()
        else -> error("")
    }

}

private fun String.translate(): String = when {
    this == "int" -> "Int"
    startsWith("java.util.Map") -> replace("java.util.Map", "MutableMap").translate()
    startsWith("java.util.List") -> replace("java.util.List", "MutableList").translate()
    contains("java.lang.String") -> replace("java.lang.String", "String?").translate()
    contains("java.lang.Integer") -> replace("java.lang.Integer", "Int?").translate()
    contains("java.lang.Long") -> replace("java.lang.Long", "Long?").translate()
    contains("java.lang.Double") -> replace("java.lang.Double", "Double?").translate()
    contains("java.lang.Object") -> replace("java.lang.Object", "Any?").translate()
    else -> this
}

