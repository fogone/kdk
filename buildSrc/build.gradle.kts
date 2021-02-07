plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.reflections:reflections:0.9.11")
    implementation("io.fabric8:kubernetes-client:5.0.1")

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
        implementation(awsCdk(it))
    }
}

fun awsCdk(name: String) = "software.amazon.awscdk:$name:1.88.0"
