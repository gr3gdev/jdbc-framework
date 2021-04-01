package com.github.gr3gdev.jdbc.dao.parser.impl

import com.github.gr3gdev.jdbc.dao.generator.element.Request
import com.github.gr3gdev.jdbc.dao.generator.element.RequestElement
import com.github.gr3gdev.jdbc.dao.parser.RequestParser
import com.github.gr3gdev.jdbc.metadata.element.TableElement
import java.util.regex.Pattern

internal class SelectParser : RequestParser {

    override fun parse(sql: String, tables: Set<TableElement>): Request {
        val selectPattern = Pattern.compile("SELECT(.+)FROM(.+)", Pattern.CASE_INSENSITIVE)
        val wherePattern = Pattern.compile("(.+)WHERE(.+)", Pattern.CASE_INSENSITIVE)
        val selectMatcher = selectPattern.matcher(sql)
        if (selectMatcher.matches()) {
            val select = selectMatcher.group(1)
            var tableName = selectMatcher.group(2)
            val selects = parseElements(tables, select, ",", RequestElement.Part.SELECT)
            val whereMatcher = wherePattern.matcher(sql)
            var where: String? = null
            if (whereMatcher.matches()) {
                where = whereMatcher.group(2)
                tableName = tableName.substring(0, tableName.indexOf("WHERE", 0, true)).trim()
            }
            val conditions = if (where != null) {
                parseConditions(tables, where)
            } else {
                emptyList()
            }
            val table = findTable(tables, tableName.trim())
            val joins = constructJoins(selects, conditions)
            val constructSql = listOf(
                    "SELECT",
                    selects.joinToString(", \" +\n${tabs()}\"") {
                        "${it.table.name}.${it.columnElement.name()} as ${it.table.name}_${it.columnElement.name()}"
                    },
                    "FROM ${table.name}")
                    .plus(joins)
                    .plus(constructSqlConditions(tables, conditions))
                    .joinToString(" ")
                    .trim()
            return Request(table, constructSql, selects.plus(conditions))
        } else {
            throw RuntimeException("Not a select query : $sql")
        }
    }

    private fun constructJoins(selects: List<RequestElement>, conditions: List<RequestElement>): Set<String> {
        return selects.plus(conditions).mapNotNull {
            if (it.parentTable != null) {
                val type = if (it.columnElement.required) {
                    "\" +\n${tabs()}\"INNER JOIN"
                } else {
                    "\" +\n${tabs()}\"LEFT JOIN"
                }
                val fkTable = it.table
                val parentTable = it.parentTable
                val parentFk = parentTable.columns.firstOrNull { c -> c.foreignKey != null && c.foreignKey!!.name == fkTable.name }
                        ?: throw RuntimeException("Foreign key not found : ${it.parentTable}")
                val fkPrimaryKey = if (parentTable.getPrimaryKey().foreignKey != null) {
                    "${parentTable.getPrimaryKey().foreignKey!!.getPrimaryKey().name()}_${parentFk.name()}"
                } else {
                    "${parentTable.getPrimaryKey().name()}_${parentFk.name()}"
                }
                "$type ${fkTable.name} ON ${fkTable.name}.${fkTable.getPrimaryKey().name()} = ${parentTable.name}.$fkPrimaryKey"
            } else {
                null
            }
        }.toSet()
    }

}