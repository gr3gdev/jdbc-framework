package com.github.gr3gdev.jdbc.dao

/**
 * Annotation for generate code to INSERT/SELECT/UPDATE/DELETE objects.
 *
 * Use "sql" attribute to specify the query.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Query(val sql: String)
