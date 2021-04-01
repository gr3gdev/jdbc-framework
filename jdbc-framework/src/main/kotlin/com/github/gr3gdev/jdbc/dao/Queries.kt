package com.github.gr3gdev.jdbc.dao

import javax.annotation.processing.Processor
import kotlin.reflect.KClass

/**
 * Annotation for a DAO interface.
 *
 * Use "implementation" attribute if you implement an abstract class.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Queries(val implementation: KClass<*> = Processor::class)
