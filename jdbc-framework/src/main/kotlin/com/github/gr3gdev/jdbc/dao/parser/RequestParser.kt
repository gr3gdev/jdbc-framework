package com.github.gr3gdev.jdbc.dao.parser

import com.github.gr3gdev.jdbc.dao.StringGenerator
import com.github.gr3gdev.jdbc.dao.generator.element.Request
import com.github.gr3gdev.jdbc.dao.generator.element.RequestElement
import com.github.gr3gdev.jdbc.metadata.element.ColumnElement
import com.github.gr3gdev.jdbc.metadata.element.TableElement
import java.util.*

internal interface RequestParser : StringGenerator {

    fun parse(sql: String, tables: Set<TableElement>): Request

    fun parseConditions(tables: Set<TableElement>, text: String): List<RequestElement> {
        return parseElements(tables, text, "AND", RequestElement.Part.WHERE)
    }

    fun constructSqlConditions(tables: Set<TableElement>, conditions: List<RequestElement>): String {
        return if (conditions.isNotEmpty()) {
            "\" +\n${tabs()}\"WHERE " + conditions.joinToString(" AND ") { "${it.table.name}.${it.columnElement.name()} = ?" }
        } else {
            ""
        }
    }

    private fun addColumns(tables: Set<TableElement>, allColumns: LinkedList<RequestElement>, base: String, table: TableElement, part: RequestElement.Part) {
        table.columns.forEach {
            if (it.foreignKey != null) {
                addColumns(tables, allColumns, "$base.${it.fieldName}", it.foreignKey!!, part)
            } else {
                val column = findColumn(tables, "$base.${it.fieldName}")
                allColumns.add(RequestElement(column.first, column.second, column.third, part))
            }
        }
    }

    fun parseElements(tables: Set<TableElement>, text: String, separator: String, part: RequestElement.Part): List<RequestElement> {
        val allColumns = LinkedList<RequestElement>()
        text.trim()
                .split(separator)
                .forEach {
                    var field = it.trim()
                    if (field.contains("WHERE", true)) {
                        field = field.substring(0, field.indexOf("WHERE", 0, true)).trim()
                    }
                    val column = findColumn(tables, field)
                    when {
                        column.second.foreignKey != null -> {
                            addColumns(tables, allColumns, field, column.second.foreignKey!!, part)
                        }
                        column.second == ColumnElement.ALL -> {
                            addColumns(tables, allColumns, field, column.first, part)
                        }
                        else -> {
                            allColumns.add(RequestElement(column.first, column.second, column.third, part))
                        }
                    }
                }
        return allColumns
    }

    fun findTable(tables: Set<TableElement>, tableName: String): TableElement {
        return tables.firstOrNull { it.fieldName.equals(tableName, true) }
                ?: throw RuntimeException("Table not found : $tableName")
    }

    fun findColumn(tables: Set<TableElement>, column: String): Triple<TableElement, ColumnElement, TableElement?> {
        val scan = Scanner(column).useDelimiter("\\.")
        var first = true
        var columns = emptyList<ColumnElement>()
        var table: TableElement? = null
        var parentTable: TableElement? = null
        var columnElement: ColumnElement? = null
        while (scan.hasNext()) {
            val part = scan.next()
            if (first) {
                // Table
                table = findTable(tables, part)
                columns = table.columns
                columnElement = ColumnElement.ALL
                first = false
            } else if (columns.isNotEmpty()) {
                // Column
                columnElement = columns.firstOrNull { it.fieldName == part }
                if (columnElement?.foreignKey != null) {
                    parentTable = table!!
                    table = columnElement.foreignKey!!
                    columns = table.columns
                }
            }
        }
        if (columnElement == null) {
            throw RuntimeException("Column not found : $column")
        }
        return Triple(table!!, columnElement, parentTable)
    }

}
