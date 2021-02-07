import ru.nobirds.kdk.deployment.k8s.generator.K8sModelGeneratorTask

plugins {
    `java-library`
}

val output = buildDir.resolve("k8s-generated")

tasks {
    val generateTask = register("generateK8sModel", K8sModelGeneratorTask::class) {
        group = "generation"
        outputDirectory.set(output)
    }
    assemble.configure {
        dependsOn(generateTask)
    }
}

java {
    sourceSets["main"].java.srcDir(output)
}
