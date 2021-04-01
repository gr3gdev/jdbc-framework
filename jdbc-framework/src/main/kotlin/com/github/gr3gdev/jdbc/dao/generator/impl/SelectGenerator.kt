package com.github.gr3gdev.jdbc.dao.generator.impl

import com.github.gr3gdev.jdbc.dao.generator.RequestGenerator
import com.github.gr3gdev.jdbc.dao.generator.element.RequestElement
import com.github.gr3gdev.jdbc.error.JDBCConfigurationException
import com.github.gr3gdev.jdbc.dao.generator.element.DaoMethodElement
import com.github.gr3gdev.jdbc.generator.element.QueryElement
import com.github.gr3gdev.jdbc.metadata.element.TableElement
import com.github.gr3gdev.jdbc.template.SelectTemplate

internal class SelectGenerator : RequestGenerator {

    private lateinit var instance: Pair<String, Boolean>

    override fun validate(queryElement: QueryElement) {
        instance = getNewInstance(queryElement.method.returnType.toString())
        if (!instance.second && !queryElement.method.returnType.toString().startsWith("java.util.Optional")) {
            throw JDBCConfigurationException("Select an unique object must return Optional<>")
        }
    }

    override fun generate(queryElement: QueryElement, tables: Set<TableElement>): DaoMethodElement {
        val methodName = queryElement.method.name
        val returnType = queryElement.method.returnType.toString()
        val parameters = constructParameters(queryElement)
        val sql = queryElement.sql
        val databaseName = queryElement.tableElement.databaseName
        val setters = constructSetters(queryElement)
        val mapAttributes = constructAttributes(queryElement, instance.second)
        val method = if (instance.second) {
            SelectTemplate.generate(methodName, parameters,
                    databaseName, sql,
                    setters, returnType, mapAttributes,
                    instance.first)
        } else {
            SelectTemplate.generate(methodName, parameters,
                    queryElement.tableElement.databaseName, sql,
                    setters, returnType, mapAttributes)
        }
        return DaoMethodElement(imports(), method)
    }

    private fun constructAttributes(queryElement: QueryElement, isCollection: Boolean): String {
        val mainObject = queryElement.request.requestElements
                .first { it.part == RequestElement.Part.SELECT }
                .table.fieldName.toLowerCase()
        val instances = queryElement.request.requestElements
                .filter { it.part == RequestElement.Part.SELECT }
                .map { it.table }
                .distinct()
                .map {
                    val tableField = it.fieldName.toLowerCase()
                    "final ${it.classType} $tableField = new ${it.classType}();"
                }
        val setters = queryElement.request.requestElements
                .filter { it.part == RequestElement.Part.SELECT }
                .map {
                    val tableField = it.table.fieldName.toLowerCase()
                    val column = it.columnElement
                    val columnAlias = "${it.table.name}_${column.name()}"
                    "$tableField.set${column.fieldName.capitalize()}(res.get${getColumnType(column)}(\"$columnAlias\"));"
                }
        val instancesSetters = queryElement.request.requestElements
                .filter {
                    it.part == RequestElement.Part.SELECT && it.parentTable != null
                            && !it.table.fieldName.equals(mainObject, true)
                }
                .map { Pair(it.table, it.parentTable!!) }
                .reversed()
                .distinct()
                .map {
                    val tableField = it.first.fieldName.toLowerCase()
                    val parentField = it.second.fieldName.toLowerCase()
                    "$parentField.set${it.first.fieldName.capitalize()}($tableField);"
                }
        return instances
                .plus(setters)
                .plus(instancesSetters)
                .plus(if (isCollection) {
                    "ret.add($mainObject);"
                } else {
                    "return java.util.Optional.of($mainObject);"
                })
                .joinToString("\n${tabs(4)}", tabs())
    }

    private fun getNewInstance(returnType: String): Pair<String, Boolean> {
        var collection = false
        val instance = when {
            returnType.startsWith("java.util.List") -> {
                collection = true
                "new java.util.LinkedList()"
            }
            returnType.startsWith("java.util.Set") -> {
                collection = true
                "new java.util.LinkedHashSet()"
            }
            else -> {
                "java.util.Optional.empty()"
            }
        }
        return Pair(instance, collection)
    }

}