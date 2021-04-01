package com.github.gr3gdev.jdbc.dao.generator.impl

import com.github.gr3gdev.jdbc.dao.generator.RequestGenerator
import com.github.gr3gdev.jdbc.error.JDBCConfigurationException
import com.github.gr3gdev.jdbc.dao.generator.element.DaoMethodElement
import com.github.gr3gdev.jdbc.generator.element.QueryElement
import com.github.gr3gdev.jdbc.metadata.element.TableElement
import com.github.gr3gdev.jdbc.template.InsertTemplate

internal class InsertGenerator : RequestGenerator {

    override fun validate(queryElement: QueryElement) {
        if (queryElement.method.returnType.toString() != "void") {
            throw JDBCConfigurationException("Query must be void method")
        }
    }

    override fun generate(queryElement: QueryElement, tables: Set<TableElement>): DaoMethodElement {
        val methodName = queryElement.method.name
        val databaseName = queryElement.tableElement.databaseName
        val sql = queryElement.sql
        val isBatch = queryElement.method.parameters.any { isCollectionParameter(it) } && queryElement.method.parameters.size == 1
        val isAutoincrement = queryElement.tableElement.getPrimaryKey().autoincrement && queryElement.method.parameters.size == 1
        val parameters = constructParameters(queryElement)
        val prefix = if (isBatch) {
            4
        } else {
            3
        }
        val setters = constructSetters(queryElement, prefix)
        val method = when {
            isBatch -> {
                val parameter = queryElement.method.parameters[0]
                val classType = queryElement.tableElement.classType
                InsertTemplate.generate(methodName, parameters, databaseName, sql, setters,
                        classType, parameter.simpleName.toString())
            }
            isAutoincrement -> {
                val parameter = queryElement.method.parameters[0]
                val setID = "${tabs(2)}${parameter.simpleName}.set${queryElement.tableElement.getPrimaryKey().fieldName.capitalize()}(res.get${getColumnType(queryElement.tableElement.getPrimaryKey())}(1));"
                InsertTemplate.generate(methodName, "final ${parameter.asType()} ${parameter.simpleName}", databaseName, sql, setters,
                        setID)
            }
            else -> {
                InsertTemplate.generate(methodName, parameters, databaseName, sql, setters)
            }
        }
        return DaoMethodElement(imports(), method)
    }

}