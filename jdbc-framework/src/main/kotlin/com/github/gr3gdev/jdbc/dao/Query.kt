package com.github.gr3gdev.jdbc.dao

/**
 * Annotation for generate code to INSERT/SELECT/UPDATE/DELETE objects.
 *
 * Use "type" attribute to specify the query type.
 *
 * Use "attributes" to specify the attribute list :
 * <ul>
 *     <li>SELECT : List of variable to select</li>
 *     <li>UPDATE : List of variable to update</li>
 *     <li>DELETE : not used</li>
 *     <li>INSERT : List of variable to insert (all if not used)</li>
 * </ul>
 *
 * Use "filters" to specify the filter list :
 * <ul>
 *     <li>SELECT : List of variable to filter in clause where</li>
 *     <li>UPDATE : List of variable to filter in clause where</li>
 *     <li>DELETE : List of variable to filter in clause where</li>
 *     <li>INSERT : not used</li>
 * </ul>
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Query(
        val type: QueryType,
        val attributes: Array<String> = ["*"],
        val filters: Array<String> = []
)
