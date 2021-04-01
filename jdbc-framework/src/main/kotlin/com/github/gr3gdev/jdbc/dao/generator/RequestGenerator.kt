package com.github.gr3gdev.jdbc.dao.generator

import com.github.gr3gdev.jdbc.dao.StringGenerator
import com.github.gr3gdev.jdbc.dao.generator.element.DaoMethodElement
import com.github.gr3gdev.jdbc.dao.generator.element.RequestElement
import com.github.gr3gdev.jdbc.generator.element.*
import com.github.gr3gdev.jdbc.metadata.element.ColumnElement
import com.github.gr3gdev.jdbc.metadata.element.TableElement
import java.lang.RuntimeException
import java.util.*
import javax.lang.model.element.VariableElement
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashSet

internal interface RequestGenerator : StringGenerator {

    companion object {
        private val mappingTypes = mapOf(
                "java.lang.String" to "String",
                "int" to "Int",
                "java.lang.Integer" to "Int",
                "java.lang.Boolean" to "Boolean",
                "boolean" to "Boolean",
                "java.lang.Long" to "Long",
                "long" to "Long",
                "java.lang.Float" to "Float",
                "float" to "Float",
                "java.lang.Double" to "Double",
                "double" to "Double",
                "java.lang.Byte" to "Byte",
                "byte" to "Byte",
                "java.lang.Short" to "Short",
                "short" to "Short",
                "java.sql.Time" to "Time",
                "java.sql.Timestamp" to "Timestamp",
                "java.sql.Date" to "Date",
                "java.util.Date" to "Date",
                "java.math.BigDecimal" to "BigDecimal",
                "java.net.URL" to "URL",
                "java.sql.Array" to "Array"
        )
    }

    fun getColumnType(col: ColumnElement): String {
        return if (col.foreignKey != null) {
            getColumnType(col.foreignKey!!.getPrimaryKey())
        } else {
            mappingTypes[col.type.toString()] ?: "Object"
        }
    }

    fun isCollectionParameter(parameter: VariableElement): Boolean {
        val type = parameter.asType().toString()
        val collections = listOf(List::class.java, ArrayList::class.java, LinkedList::class.java,
                Set::class.java, HashSet::class.java, SortedSet::class.java, LinkedHashSet::class.java)
        return collections.any { type.startsWith(it.canonicalName) }
    }

    fun getParameter(method: MethodElement, tableElement: TableElement, column: ColumnElement): String {
        var parameter = method.parameters.firstOrNull {
            it.simpleName.toString().equals(column.fieldName, true)
        }
        if (parameter != null) {
            return parameter.simpleName.toString()
        }
        parameter = method.parameters.first {
            it.simpleName.toString().equals(tableElement.name, true)
                    || isCollectionParameter(it) && it.asType().toString().contains("<${tableElement.classType}>", true)
        }
        val elt = if (isCollectionParameter(parameter)) {
            "element"
        } else {
            parameter.simpleName.toString()
        }
        return if (column.foreignKey != null) {
            "$elt.get${column.fieldName.capitalize()}().get${column.foreignKey!!.getPrimaryKey().fieldName.capitalize()}()"
        } else {
            "$elt.get${column.fieldName.capitalize()}()"
        }
    }

    fun setters(list: List<String>, method: MethodElement, tableElement: TableElement, nbTabs: Int = 3): String {
        var index = 1
        return list.joinToString("\n${tabs(nbTabs)}") {
            val column = tableElement.getColumn(it)
            "stm.set${getColumnType(column)}(${index++}, ${getParameter(method, tableElement, column)});"
        }
    }

    fun imports(vararg imports: String) = hashSetOf("java.sql.Connection", "java.sql.PreparedStatement",
            "java.sql.ResultSet", "java.sql.SQLException", "java.sql.Statement").plus(imports)

    fun validate(queryElement: QueryElement)

    fun generate(queryElement: QueryElement, tables: Set<TableElement>): DaoMethodElement

    private fun findVariable(request: RequestElement, method: MethodElement): String {
        val variableWithColumnName = method.parameters.firstOrNull {
            it.simpleName.toString().equals(request.columnElement.fieldName, true)
        }
        return if (variableWithColumnName == null) {
            val variableWithTableName = method.parameters.firstOrNull {
                it.simpleName.toString().equals(request.table.fieldName, true)
                        || it.asType().toString() == request.table.classType
                        || isCollectionParameter(it) && it.asType().toString().contains(request.table.classType)
            }
            if (variableWithTableName == null) {
                val variableWithParentTableName = method.parameters.firstOrNull {
                    it.simpleName.toString().equals(request.parentTable?.fieldName, true)
                            && it.asType().toString() == request.parentTable?.classType
                } ?: throw RuntimeException("No parameter found for : ${request.columnElement.fieldName}")
                val name = getVariableName(variableWithParentTableName)
                "$name.get${request.table.fieldName.capitalize()}().get${request.columnElement.fieldName.capitalize()}()"
            } else {
                val name = getVariableName(variableWithTableName)
                if (request.columnElement.foreignKey != null) {
                    "$name.get${request.columnElement.fieldName.capitalize()}().get${request.columnElement.foreignKey!!.getPrimaryKey().fieldName.capitalize()}()"
                } else {
                    "$name.get${request.columnElement.fieldName.capitalize()}()"
                }
            }
        } else {
            variableWithColumnName.simpleName.toString()
        }
    }

    fun getVariableName(variableWithTableName: VariableElement): String {
        return if (isCollectionParameter(variableWithTableName)) {
            "element"
        } else {
            variableWithTableName.simpleName.toString()
        }
    }

    fun constructParameters(queryElement: QueryElement): String {
        return queryElement.method.parameters.joinToString(", ") {
            "final ${it.asType()} ${it.simpleName}"
        }
    }

    fun constructSetters(queryElement: QueryElement, nbTabs: Int = 3): String {
        var index = 1
        val setters = queryElement.request.requestElements
                .filter { it.part != RequestElement.Part.SELECT }
                .joinToString("\n${tabs(nbTabs)}") {
                    val variable = findVariable(it, queryElement.method)
                    "stm.set${getColumnType(it.columnElement)}(${index++}, $variable);"
                }
        return if (setters.isNotBlank()) {
            "${tabs(nbTabs - 1)}$setters"
        } else {
            "${tabs(nbTabs - 1)}// Without parameters"
        }
    }

}