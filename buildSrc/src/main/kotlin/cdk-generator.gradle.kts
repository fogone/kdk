import ru.nobirds.kdk.deployment.cdk.CdkModelGeneratorTask
import org.gradle.kotlin.dsl.*

plugins {
    `java-library`
}

val output = buildDir.resolve("cdk-generated")

tasks {
    val generateTask = register<CdkModelGeneratorTask>("generateCdkModel") {
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

dependencies {
    listOf(
        "apigateway",
        "apigatewayv2",
        "appsync",
        "autoscaling",
        "batch",
        "cdk-assets",
        "cdk-customresources",
        "ce",
        "certificatemanager",
        "cloudformation",
        "cloudfront",
        "cloudfront-origins",
        "cloudtrail",
        "cloudwatch",
        "cloudwatch-actions",
        "codebuild",
        "codecommit",
        "codedeploy",
        "codepipeline",
        "codepipeline-actions",
        "cognito",
        "core",
        "datapipeline",
        "dynamodb",
        "ec2",
        "ecr",
        "elasticsearch",
        "events",
        "glue",
        "kms",
        "lambda",
        "lambda-event-sources",
        "neptune",
        "rds",
        "route53",
        "route53-patterns",
        "route53-targets",
        "s3",
        "s3-assets",
        "s3-deployment",
        "sam",
        "secretsmanager",
        "ses",
        "sns",
        "sqs",
        "ssm",
        "stepfunctions",
        "stepfunctions-tasks"
    ).forEach {
        compileOnly(awsCdk(it))
    }
}

fun awsCdk(name: String) = "software.amazon.awscdk:$name:1.88.0"
