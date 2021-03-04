package com.github.gr3gdev.jdbc.metadata

/**
 * Annotation for create a table.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Table(
        val databaseName: String = "default"
)