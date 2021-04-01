package com.github.gr3gdev.jdbc.dao.generator.impl

import com.github.gr3gdev.jdbc.dao.generator.RequestGenerator
import com.github.gr3gdev.jdbc.error.JDBCConfigurationException
import com.github.gr3gdev.jdbc.dao.generator.element.DaoMethodElement
import com.github.gr3gdev.jdbc.generator.element.QueryElement
import com.github.gr3gdev.jdbc.metadata.element.TableElement
import com.github.gr3gdev.jdbc.template.DeleteOrUpdateTemplate

internal class DeleteGenerator : RequestGenerator {

    override fun validate(queryElement: QueryElement) {
        if (queryElement.method.returnType.toString() != "int") {
            throw JDBCConfigurationException("Query must be return int")
        }
    }

    override fun generate(queryElement: QueryElement, tables: Set<TableElement>): DaoMethodElement {
        val methodName = queryElement.method.name
        val parameters = constructParameters(queryElement)
        val databaseName = queryElement.tableElement.databaseName
        val sql = queryElement.sql
        val setters = constructSetters(queryElement)
        val method = DeleteOrUpdateTemplate.generate(methodName, parameters, databaseName, sql, setters)
        return DaoMethodElement(imports(), method)
    }

}