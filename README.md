# Kotlin Deployment Kit
## Aws Cdk Kotlin Dsl
Kotlin DSL for official library for AWS CDK. Table contains only versions where cdk version was changed. 

| KDK Version  | CDK Version  |
|--------------|--------------|
| 0.1          | 1.88.0       |

## Aws Cdk Kotlin Gradle Plugin

Simple example:
```kotlin
plugins {
    id("ru.nobirds.kdk.cdk") version "0.1.2"
}

cdk {
    awsProfile("default")

    dependency(":module:build")

    application {
        buildStack("my-stack") {
            val api = restApi("my-api") {
                deployOptions { stageName("default") }
                // for some lambdas we should specify type, that's why this function
                // defined with fun-syntax to resolve kotlin conflict between one-method interface
                // and simple lambda extension
                endpointConfiguration(fun EndpointConfiguration.Builder.() {
                    types(EndpointType.EDGE)
                })
            }

            // todo
        }
    }
}
```

## Kubernetes Kotlin Dsl
Kotlin DSL for Fabric8 Kubernetes client

| KDK Version  | Fabric8 Version  |
|--------------|------------------|
| 0.1          | 5.0.1            |

