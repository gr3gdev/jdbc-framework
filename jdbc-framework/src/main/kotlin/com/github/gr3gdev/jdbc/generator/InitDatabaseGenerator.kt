package com.github.gr3gdev.jdbc.generator

import com.github.gr3gdev.jdbc.processor.ReflectUtils
import javax.lang.model.element.AnnotationMirror

object InitDatabaseGenerator {

    fun generate(confAnnotation: AnnotationMirror): String {
        val configFile = ReflectUtils.getAnnotationAttributeValue(confAnnotation, "configFile")
        val databaseName = ReflectUtils.getAnnotationAttributeValue(confAnnotation, "databaseName") ?: "default"
        return "SQLDataSource.init(\"$configFile\", \"$databaseName\");"
    }

}