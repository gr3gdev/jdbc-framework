package com.github.gr3gdev.jdbc.metadata.element

internal abstract class DatabaseElement {

    private val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()

    fun String.camelToSnakeCase(): String {
        return camelRegex.replace(this) {
            "_${it.value}"
        }.toUpperCase()
    }

}