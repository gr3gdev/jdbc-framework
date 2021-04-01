package com.github.gr3gdev.jdbc.dao.parser.impl

import com.github.gr3gdev.jdbc.dao.generator.element.Request
import com.github.gr3gdev.jdbc.dao.parser.RequestParser
import com.github.gr3gdev.jdbc.metadata.element.TableElement
import java.util.regex.Pattern

internal class DeleteParser : RequestParser {

    override fun parse(sql: String, tables: Set<TableElement>): Request {
        val pattern = Pattern.compile("DELETE(.+)", Pattern.CASE_INSENSITIVE)
        val wherePattern = Pattern.compile("(.+)WHERE(.+)", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(sql)
        if (matcher.matches()) {
            var tableName = matcher.group(1).trim()
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
            val constructSql = listOf("DELETE FROM ${table.name}")
                    .plus(constructSqlConditions(tables, conditions))
                    .joinToString(" ")
                    .trim()
            return Request(table, constructSql, conditions)
        } else {
            throw RuntimeException("Not a delete query : $sql")
        }
    }

}