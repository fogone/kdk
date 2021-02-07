package ru.nobirds.kdk.cdk.dsl

@DslMarker
@Target(AnnotationTarget.TYPE, AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class CdkDsl()
