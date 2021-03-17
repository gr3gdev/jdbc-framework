package com.github.gr3gdev.jdbc.generator.element

import javax.lang.model.type.TypeMirror

internal class ColumnElement(
        val fieldName: String,
        val type: TypeMirror,
        val primaryKey: Boolean,
        val autoincrement: Boolean
) : DatabaseElement() {
    var foreignKey: TableElement? = null
    fun name() = fieldName.camelToSnakeCase()
}