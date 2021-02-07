package ru.nobirds.kdk.deployment.cdk

import ru.nobirds.kdk.deployment.firstToLowerCase
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import software.amazon.awscdk.core.Construct
import software.amazon.jsii.Builder
import java.io.OutputStreamWriter
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.WildcardType
import java.util.concurrent.Executors

open class CdkModelGeneratorTask() : DefaultTask() {

    @OutputDirectory
    val outputDirectory = project.objects.directoryProperty()

    @TaskAction
    fun generate() {
        val executorService = Executors.newFixedThreadPool(8)

        val reflections = Reflections(ConfigurationBuilder()
                .addUrls(ClasspathHelper.forPackage("software.amazon.awscdk"))
                .addScanners(SubTypesScanner())
                .setExecutorService(executorService)
        )

        val ignore = setOf("App")

        val constructs = reflections.getSubTypesOf(Construct::class.java)
                .filter { "$" !in it.canonicalName && !it.kotlin.isAbstract && it.simpleName !in ignore && it.constructors.any { it.parameterCount == 3 } }

        val output = outputDirectory.asFile.get()
        output.mkdirs()

        constructs.groupBy { it.getPackage().name }.forEach { (pkg, types) ->
            val group = pkg.substringAfterLast("software.amazon.awscdk.")

            val groupDir = output.resolve(group)
            groupDir.mkdirs()

            groupDir.resolve("construct.kt").writer().use { output ->
                output.appendln("package ru.nobirds.kdk.cdk.dsl.$group")
                output.appendln()
                output.appendln("import ru.nobirds.kdk.cdk.dsl.*")
                output.appendln("import software.amazon.awscdk.core.Construct")
                output.appendln()
                for (construct in types) {
                    generateConstructBuilders(construct, output)
                }
            }
        }

        val builders = reflections.getSubTypesOf(Builder::class.java)
            .filter { it.isMemberClass && "$" !in it.declaringClass.canonicalName
                    && (it.declaringClass.methods.any { Modifier.isStatic(it.modifiers) && it.name == "builder" }
                    || it.methods.any { Modifier.isStatic(it.modifiers) && it.name == "create" && it.parameterCount == 0 })}

        builders.map { it.declaringClass }.groupBy { it.getPackage().name }.forEach { (pkg, types) ->
            val group = pkg.substringAfterLast("software.amazon.awscdk.")

            val groupDir = output.resolve(group)
            groupDir.mkdirs()

            groupDir.resolve("builders.kt").writer().use { output ->
                output.appendln("package ru.nobirds.kdk.cdk.dsl.$group")
                output.appendln()
                output.appendln("import ru.nobirds.kdk.cdk.dsl.*")
                output.appendln()
                for (type in types) {
                    generateStandaloneBuilders(type, output)
                }
            }
        }

        builders.groupBy { it.getPackage().name }.forEach { (pkg, types) ->
            val group = pkg.substringAfterLast("software.amazon.awscdk.")

            val groupDir = output.resolve(group)
            groupDir.mkdirs()

            groupDir.resolve("buildersProps.kt").writer().use { output ->
                output.appendln("package ru.nobirds.kdk.cdk.dsl.$group")
                output.appendln()
                output.appendln("import ru.nobirds.kdk.cdk.dsl.*")
                output.appendln()
                for (type in types) {
                    generateStandaloneBuildersProps(type, output)
                }
            }
        }
    }

    private fun generateStandaloneBuildersProps(type: Class<out Builder<*>>, output: OutputStreamWriter) {
        val deprecated = type.annotations.any { it.annotationClass == java.lang.Deprecated::class }

        type.methods
            .filter {"$" !in it.name }
            .filter { it.parameterCount == 1 &&
                    (it.parameterTypes.first().isBuildable() || it.parameterTypes.first() == List::class.java)
            }
            .groupBy { it.name }.forEach { (name, methods) ->
                if (methods.size == 1) {
                    val method = methods.first()
                    val builder = method.parameterTypes.first()
                    if (builder == List::class.java) {
                        val builderType = method.genericParameterTypes[0] as ParameterizedType
                        val parameter = builderType.actualTypeArguments[0]
                        if (parameter is Class<*>) {
                            val parameterTypeName = parameter.kotlin.qualifiedName

                            if(deprecated) output.appendln("@Deprecated(message = \"Deprecated in AWS CDK\")")
                            output.append("inline fun ${type.declaringClass.canonicalName}.Builder.$name(vararg items: $parameterTypeName) ")
                            output.appendln("{ $name(items.toList()) }")
                        } else if(parameter is WildcardType) {
                            val upperBound = parameter.upperBounds[0]
                            if (upperBound is Class<*>) {
                                val parameterTypeName = upperBound.kotlin.qualifiedName

                                if (upperBound.isBuildable()) {
                                    if(deprecated) output.appendln("@Deprecated(message = \"Deprecated in AWS CDK\")")
                                    output.append("inline fun ${type.declaringClass.canonicalName}.Builder.$name(builder: @CdkDsl ItemsBuilder<${parameterTypeName}.Builder.() -> Unit>.() -> Unit = {}) ")
                                    output.appendln("{ $name(buildItems(builder).map { ${parameterTypeName}.builder().apply(it).build() }) }")
                                } else {
                                    if(deprecated) output.appendln("@Deprecated(message = \"Deprecated in AWS CDK\")")
                                    output.append("inline fun ${type.declaringClass.canonicalName}.Builder.$name(vararg items: ${parameterTypeName}) ")
                                    output.appendln("{ $name(items.toList()) }")
                                }
                            } else {
                                // todo
                            }
                        }
                    } else {
                        if(deprecated) output.appendln("@Deprecated(message = \"Deprecated in AWS CDK\")")
                        output.append("inline fun ${type.declaringClass.name}.Builder.$name(builder: @CdkDsl ${builder.name}.Builder.() -> Unit = {}) ")
                        output.appendln("{ $name(${builder.name}.builder().apply(builder).build()) }")
                    }
                } else {
                    // todo
                }
        }
    }

    private fun generateConstructBuilders(construct: Class<out Construct>, output: OutputStreamWriter) {
        val typeName = construct.canonicalName
        val name = construct.simpleName.firstToLowerCase()
        val propsTypeName = "${typeName}Props"
        val propsName = "${name}Props"
        val deprecated = construct.annotations.any { it.annotationClass == java.lang.Deprecated::class }

        if(deprecated) output.appendln("@Deprecated(message = \"Deprecated in AWS CDK\")")
        output.appendln("inline fun Construct.$name(id: String, props: @CdkDsl ${propsTypeName}.Builder.() -> Unit = {}): $typeName = $typeName(this, id, ${propsName}(props))")
        if(deprecated) output.appendln("@Deprecated(message = \"Deprecated in AWS CDK\")")
        output.appendln("inline fun Construct.build${construct.simpleName}(id: String, props: $propsTypeName = ${propsTypeName}.builder().build(), builder: @CdkDsl $typeName.() -> Unit): $typeName = $typeName(this, id, props).apply(builder)")
        output.appendln()
    }

    private fun generateStandaloneBuilders(type: Class<*>, output: OutputStreamWriter) {
        val name = type.simpleName.firstToLowerCase()
        val deprecated = type.annotations.any { it.annotationClass == java.lang.Deprecated::class }

        val builderConstructor = if(type.methods.any { it.name == "builder" && it.parameterCount == 0 }) "${type.canonicalName}.builder()" else "${type.canonicalName}.Builder.create()"

        if(deprecated) output.appendln("@Deprecated(message = \"Deprecated in AWS CDK\")")
        output.appendln("inline fun ${name}(builder: @CdkDsl ${type.canonicalName}.Builder.() -> Unit = {}): ${type.canonicalName} = ${builderConstructor}.apply(builder).build()")
        output.appendln()
    }

    private fun Class<*>.isBuildable(): Boolean {
        return name.startsWith("software.amazon.awscdk.")
                && declaringClass == null
                && methods.any { Modifier.isStatic(it.modifiers) && it.name == "builder" }
    }
}
