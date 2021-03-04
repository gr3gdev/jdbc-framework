package com.github.gr3gdev.jdbc.dao

import kotlin.reflect.KClass

/**
 * Annotation for a DAO interface.
 *
 * Use "mapTo" attribute to associate the @Table class.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Queries(val mapTo: KClass<*>)
