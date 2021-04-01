package com.github.gr3gdev.jdbc.dao.parser.impl

import com.github.gr3gdev.jdbc.dao.generator.element.Request
import com.github.gr3gdev.jdbc.dao.generator.element.RequestElement
import com.github.gr3gdev.jdbc.dao.parser.RequestParser
import com.github.gr3gdev.jdbc.metadata.element.TableElement
import java.util.regex.Pattern

internal class InsertParser : RequestParser {

    override fun parse(sql: String, tables: Set<TableElement>): Request {
        val pattern = Pattern.compile("INSERT(.+)\\((.+)\\)", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(sql)
        if (matcher.matches()) {
            val table = findTable(tables, matcher.group(1).trim())
            val values = matcher.group(2)
            val elements = parseElements(tables, values, ",", RequestElement.Part.INSERT).filter {
                !it.columnElement.autoincrement
                        && (it.table == table || it.parentTable == table)
            }.map {
                if (it.parentTable == table) {
                    val fkColum = table.getColumn(it.table.fieldName)
                    RequestElement(table, fkColum, null, RequestElement.Part.UPDATE)
                } else {
                    it
                }
            }.distinctBy { it.columnElement.name() }
            val constructSql = "INSERT INTO ${table.name} (${
                elements.joinToString(", ") {
                    if (it.columnElement.foreignKey != null) {
                        "${it.columnElement.foreignKey!!.getPrimaryKey().name()}_${it.columnElement.name()}"
                    } else {
                        it.columnElement.name()
                    }
                }
            }) VALUES (${elements.joinToString(", ") { "?" }})"
            return Request(table, constructSql, elements)
        } else {
            throw RuntimeException("Not an insert query : $sql")
        }
    }

}