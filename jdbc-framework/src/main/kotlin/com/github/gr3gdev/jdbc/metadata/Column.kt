package com.github.gr3gdev.jdbc.metadata

/**
 * Annotation for create a column.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class Column(
        val name: String = "",
        val primaryKey: Boolean = false,
        val autoincrement: Boolean = false,
        val required: Boolean = true
)