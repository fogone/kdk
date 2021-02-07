package ru.nobirds.kdk.deployment

fun String.firstToLowerCase(): String = get(0).toLowerCase() + substring(1)
fun String.firstToUpperCase(): String = get(0).toUpperCase() + substring(1)

