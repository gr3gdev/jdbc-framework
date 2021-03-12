package com.github.gr3gdev.jdbc.processor

import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import kotlin.reflect.KClass

internal object ReflectUtils {

    fun isAnnotationPresent(element: Element, clazz: KClass<*>): Boolean {
        return element.annotationMirrors
                .any { it.annotationType.toString() == clazz.qualifiedName }
    }

    fun getAnnotation(element: Element, clazz: KClass<*>): AnnotationMirror {
        return element.annotationMirrors
                .firstOrNull { it.annotationType.toString() == clazz.qualifiedName }
                ?: throw RuntimeException("Annotation ${clazz.simpleName} not found in ${element.simpleName}")
    }

    fun getAnnotationAttributeValue(annotation: AnnotationMirror, attribute: String): Any? {
        return annotation.elementValues.entries
                .firstOrNull { it.key.simpleName.toString() == attribute }?.value?.value
    }

}