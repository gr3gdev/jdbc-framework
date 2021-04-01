package com.github.gr3gdev.jdbc.dao.parser.impl

import com.github.gr3gdev.jdbc.dao.generator.element.Request
import com.github.gr3gdev.jdbc.dao.generator.element.RequestElement
import com.github.gr3gdev.jdbc.dao.parser.RequestParser
import com.github.gr3gdev.jdbc.metadata.element.TableElement
import java.util.regex.Pattern

internal class UpdateParser : RequestParser {

    override fun parse(sql: String, tables: Set<TableElement>): Request {
        val pattern = Pattern.compile("UPDATE(.+)SET(.+)", Pattern.CASE_INSENSITIVE)
        val wherePattern = Pattern.compile("(.+)WHERE(.+)", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(sql)
        if (matcher.matches()) {
            val tableName = matcher.group(1)
            val sets = matcher.group(2)
            val whereMatcher = wherePattern.matcher(sql)
            var where: String? = null
            if (whereMatcher.matches()) {
                where = whereMatcher.group(2)
            }
            val conditions = if (where != null) {
                parseConditions(tables, where)
            } else {
                emptyList()
            }
            val table = findTable(tables, tableName.trim())
            val setters = parseElements(tables, sets, ",", RequestElement.Part.UPDATE).filter {
                !it.columnElement.autoincrement
                        && conditions.none { c -> c.columnElement == it.columnElement }
                        && (it.table == table || it.parentTable == table)
            }.map {
                if (it.parentTable == table) {
                    val fkColum = table.getColumn(it.table.fieldName)
                    RequestElement(table, fkColum, null, RequestElement.Part.UPDATE)
                } else {
                    it
                }
            }.distinct()
            val constructSql = listOf("UPDATE ${table.name} SET",
                    setters.joinToString(", ") {
                        if (it.columnElement.foreignKey != null) {
                            "${it.columnElement.foreignKey!!.getPrimaryKey().name()}_${it.columnElement.name()} = ?"
                        } else {
                            "${it.columnElement.name()} = ?"
                        }
                    },
                    constructSqlConditions(tables, conditions))
                    .joinToString(" ")
                    .trim()
            return Request(table, constructSql, setters.plus(conditions))
        } else {
            throw RuntimeException("Not an update query : $sql")
        }
    }

}