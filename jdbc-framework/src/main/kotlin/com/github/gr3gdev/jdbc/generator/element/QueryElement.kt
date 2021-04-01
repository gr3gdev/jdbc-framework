package com.github.gr3gdev.jdbc.generator.element

import com.github.gr3gdev.jdbc.dao.QueryType
import com.github.gr3gdev.jdbc.dao.generator.element.DaoMethodElement
import com.github.gr3gdev.jdbc.dao.generator.element.Request
import com.github.gr3gdev.jdbc.metadata.element.TableElement

internal class QueryElement(
        private val sqlCode: String,
        val method: MethodElement
) {

    lateinit var tableElement: TableElement
    lateinit var request: Request
    lateinit var sql: String

    fun generate(tables: Set<TableElement>): DaoMethodElement {
        val queryType = QueryType.findByRequest(sqlCode)
        request = queryType.parser.parse(sqlCode, tables)
        tableElement = request.table
        sql = request.sql
        val queryGenerator = queryType.generator
        queryGenerator.validate(this)
        return queryGenerator.generate(this, tables)
    }

}
