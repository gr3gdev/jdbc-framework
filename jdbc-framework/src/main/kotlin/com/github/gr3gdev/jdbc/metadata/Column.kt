package com.github.gr3gdev.jdbc.metadata

/**
 * Annotation for create a column.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class Column(
        val primaryKey: Boolean = false,
        val autoincrement: Boolean = false
)