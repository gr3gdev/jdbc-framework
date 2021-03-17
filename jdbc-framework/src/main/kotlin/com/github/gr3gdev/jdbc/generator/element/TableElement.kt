package com.github.gr3gdev.jdbc.generator.element

import com.github.gr3gdev.jdbc.metadata.Column
import com.github.gr3gdev.jdbc.metadata.Table
import com.github.gr3gdev.jdbc.processor.ReflectUtils
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

internal class TableElement(private val processingEnv: ProcessingEnvironment, table: Element, val classType: String?) : DatabaseElement() {

    val name = table.simpleName.toString().camelToSnakeCase()
    val databaseName = ReflectUtils.getAnnotationAttributeValue(ReflectUtils.getAnnotation(table, Table::class), "databaseName") ?: "default"
    val columns = getColumns(table)

    fun getPrimaryKey() = columns.firstOrNull { it.primaryKey } ?: throw RuntimeException("No primary key found for $name")

    private fun getColumns(table: Element): List<ColumnElement> {
        return table.enclosedElements.filter { ReflectUtils.isAnnotationPresent(it, Column::class) }
                .map {
                    val annotation = ReflectUtils.getAnnotation(it, Column::class)
                    val ce = ColumnElement(
                            it.simpleName.toString(),
                            it.asType(),
                            ReflectUtils.getAnnotationAttributeValue(annotation, "primaryKey") as Boolean? ?: false,
                            ReflectUtils.getAnnotationAttributeValue(annotation, "autoincrement") as Boolean? ?: false
                    )
                    val columnType = processingEnv.typeUtils.asElement(it.asType())
                    if (columnType != null && ReflectUtils.isAnnotationPresent(columnType, Table::class)) {
                        ce.foreignKey = TableElement(processingEnv, columnType, columnType.asType().toString())
                    }
                    ce
                }
    }

    fun getColumn(colName: String) = columns.firstOrNull { it.fieldName.equals(colName, true) }
            ?: throw RuntimeException("Column $colName not found in table $name")

}