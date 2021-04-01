package com.github.gr3gdev.jdbc.metadata.element

import javax.lang.model.element.AnnotationMirror
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.lang.model.type.TypeVisitor

internal class ColumnElement(
        val fieldName: String,
        val type: TypeMirror,
        val primaryKey: Boolean,
        val autoincrement: Boolean,
        val required: Boolean
) : DatabaseElement() {
    var foreignKey: TableElement? = null
    fun name() = fieldName.camelToSnakeCase()

    companion object {
        val ALL = ColumnElement("*", object : TypeMirror {
            override fun getAnnotationMirrors() = emptyList<AnnotationMirror>()
            override fun <A : Annotation?> getAnnotation(annotationType: Class<A>?) = null
            override fun <A : Annotation?> getAnnotationsByType(annotationType: Class<A>?) = null
            override fun getKind(): TypeKind? = null
            override fun <R : Any?, P : Any?> accept(v: TypeVisitor<R, P>?, p: P) = null
        }, primaryKey = true, autoincrement = false, required = false)
    }
}