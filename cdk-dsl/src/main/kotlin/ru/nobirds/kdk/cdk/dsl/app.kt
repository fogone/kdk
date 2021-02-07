package ru.nobirds.kdk.cdk.dsl

import software.amazon.awscdk.core.App
import software.amazon.awscdk.core.AppProps
import software.amazon.awscdk.core.StageSynthesisOptions

fun app(props: AppProps? = null, builder: App.() -> Unit): App {
    return App(props).apply(builder)
}

fun App.synth(builder: StageSynthesisOptions.Builder.() -> Unit) {
    this.synth(StageSynthesisOptions.builder().apply(builder).build())
}
