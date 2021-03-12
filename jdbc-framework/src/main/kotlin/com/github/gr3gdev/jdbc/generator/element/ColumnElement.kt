package com.github.gr3gdev.jdbc.generator.element

import javax.lang.model.type.TypeMirror

internal class ColumnElement(
        val tableName: String,
        val fieldName: String,
        val type: TypeMirror,
        val primaryKey: Boolean,
        val autoincrement: Boolean,
        val required: Boolean,
        val sqlType: String
) : DatabaseElement() {
    var foreignKey: TableElement? = null
    fun name() = fieldName.camelToSnakeCase()
}