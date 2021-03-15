package com.github.gr3gdev.jdbc.dao

import kotlin.reflect.KClass

/**
 * Annotation for specified join type in query.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class QueryJoin(
        val type: QueryJoinType,
        val table: KClass<*>
)
