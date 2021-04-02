package com.github.gr3gdev.jdbc.metadata.element

import com.github.gr3gdev.jdbc.metadata.Column
import com.github.gr3gdev.jdbc.metadata.Table
import com.github.gr3gdev.jdbc.processor.ReflectUtils
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

internal class TableElement(private val processingEnv: ProcessingEnvironment, table: Element, val classType: String) : DatabaseElement() {

    private val annotation = ReflectUtils.getAnnotation(table, Table::class)

    val fieldName = table.simpleName.toString()
    val name: String
    val databaseName = ReflectUtils.getAnnotationAttributeValue(annotation, "databaseName") as String? ?: "default"
    val columns = getColumns(table)

    init {
        val tableName = ReflectUtils.getAnnotationAttributeValue(annotation, "name") as String?
        name = if (tableName.isNullOrBlank()) {
            fieldName.camelToSnakeCase()
        } else {
            tableName
        }
    }

    fun getPrimaryKey() = columns.firstOrNull { it.primaryKey }
            ?: throw RuntimeException("No primary key found for $name")

    private fun getColumns(table: Element): List<ColumnElement> {
        return table.enclosedElements.filter { ReflectUtils.isAnnotationPresent(it, Column::class) }
                .map {
                    val annotation = ReflectUtils.getAnnotation(it, Column::class)
                    val ce = ColumnElement(
                            it.simpleName.toString(),
                            it.asType(),
                            ReflectUtils.getAnnotationAttributeValue(annotation, "primaryKey") as Boolean? ?: false,
                            ReflectUtils.getAnnotationAttributeValue(annotation, "autoincrement") as Boolean? ?: false,
                            ReflectUtils.getAnnotationAttributeValue(annotation, "required") as Boolean? ?: true,
                            ReflectUtils.getAnnotationAttributeValue(annotation, "name") as String?
                    )
                    val columnType = processingEnv.typeUtils.asElement(it.asType())
                    if (columnType != null && ReflectUtils.isAnnotationPresent(columnType, Table::class)) {
                        ce.foreignKey = TableElement(processingEnv, columnType, columnType.asType().toString())
                    }
                    ce
                }
    }

    fun getColumn(colName: String) = columns.firstOrNull { it.name().equals(colName, true) }
            ?: columns.firstOrNull { it.foreignKey != null && it.foreignKey!!.fieldName.equals(colName, true) }
            ?: throw RuntimeException("Column $colName not found in table $name")

}