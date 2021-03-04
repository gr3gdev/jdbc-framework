package com.github.gr3gdev.jdbc.generator

import com.github.gr3gdev.jdbc.dao.Query
import com.github.gr3gdev.jdbc.dao.QueryType
import com.github.gr3gdev.jdbc.generator.element.TableElement
import com.github.gr3gdev.jdbc.generator.impl.DeleteGenerator
import com.github.gr3gdev.jdbc.generator.impl.InsertGenerator
import com.github.gr3gdev.jdbc.generator.impl.SelectGenerator
import com.github.gr3gdev.jdbc.generator.impl.UpdateGenerator
import com.github.gr3gdev.jdbc.processor.JDBCProcessor
import com.github.gr3gdev.jdbc.processor.ReflectUtils
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.VariableElement
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

internal abstract class QueryGenerator(private val tableElement: TableElement) {

    protected val mappingTypes = mapOf(
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

    protected fun imports(vararg imports: String) = listOf("java.sql.Connection", "java.sql.PreparedStatement",
            "java.sql.ResultSet", "java.sql.SQLException", "java.sql.Statement").plus(imports)

    abstract fun execute(element: Element, attributes: List<String>?, filters: List<String>?): Pair<List<String>, String>

    protected fun getConnection() = "final Connection cnx = SQLDataSource.getConnection(\"${tableElement.databaseName}\")"

    protected fun getFilters(filters: List<String>) = filters.joinToString(" AND ") {
        "${tableElement.getColumn(it).name()} = ?"
    }

    protected fun setParameters(params: List<String>?): String? {
        val tab = JDBCProcessor.TAB
        var index = 1
        return params?.joinToString("\n$tab$tab$tab$tab") {
            setParameter(index++, it)
        }
    }

    protected fun setParameters(params: List<String>?, query: ExecutableElement): String? {
        val tab = JDBCProcessor.TAB
        var index = 1
        return params?.joinToString("\n$tab$tab$tab") {
            setParameter(index++, it, query.parameters)
        }
    }

    private fun setParameter(index: Int, att: String): String {
        val col = tableElement.getColumn(att)
        var type = mappingTypes[col.type.toString()] ?: "Object"
        var value = "element.get${att.capitalize()}()"
        if (col.foreignKey != null) {
            type = mappingTypes[col.foreignKey!!.type.toString()] ?: "Object"
            value += ".get${col.foreignKey!!.fieldName.capitalize()}()"
        }
        return "stm.set$type($index, $value);"
    }

    private fun setParameter(index: Int, att: String, parameters: MutableList<out VariableElement>): String {
        val col = tableElement.getColumn(att)
        var type = mappingTypes[col.type.toString()] ?: "Object"
        var value = parameters.firstOrNull { it.simpleName.toString().equals(att, true) }?.toString()
        if (value == null && parameters.size == 1) {
            value = "${parameters[0].simpleName}.get${att.capitalize()}()"
        }
        if (col.foreignKey != null) {
            type = mappingTypes[col.foreignKey!!.type.toString()] ?: "Object"
            value += ".get${col.foreignKey!!.fieldName.capitalize()}()"
        }
        return "stm.set$type($index, $value);"
    }

    protected fun getResult(att: String, obj: String, fk: String? = null): String {
        val col = tableElement.getColumn(att)
        var colName = col.name()
        if (fk != null) {
            colName += "_$fk"
        }
        return "$obj.set${att.capitalize()}(res.get${mappingTypes[col.type.toString()] ?: "Object"}(\"$colName\"));"
    }

    protected fun joinParameters(query: ExecutableElement) = query.parameters.joinToString(", ") { "final ${it.asType()} ${it.simpleName}" }

    protected fun throwException(type: String, parameters: MutableList<out VariableElement>): String {
        return "throw new JDBCExecutionException($type, \"${
            parameters.joinToString(", ") {
                it.simpleName.toString()
            }
        }\", throwables);"
    }

    fun isCollectionParameter(parameter: VariableElement): Boolean {
        val type = parameter.asType().toString()
        val collections = listOf(List::class.java, ArrayList::class.java, LinkedList::class.java,
                Set::class.java, HashSet::class.java, SortedSet::class.java)
        return collections.any { type.startsWith(it.canonicalName) }
    }

    companion object {

        fun generate(processingEnv: ProcessingEnvironment, table: Element, type: Any?, query: Element): Pair<List<String>, String> {
            val tableElement = TableElement(processingEnv, table, type?.toString())
            val queryAnnotation = ReflectUtils.getAnnotation(query, Query::class)
            val queryType = ReflectUtils.getAnnotationAttributeValue(queryAnnotation, "type")
            val attributes = ReflectUtils.getAnnotationAttributeValue(queryAnnotation, "attributes") as List<*>?
            val filters = ReflectUtils.getAnnotationAttributeValue(queryAnnotation, "filters") as List<*>?
            return when {
                queryType.toString() == QueryType.SELECT.name -> {
                    SelectGenerator(tableElement)
                }
                queryType.toString() == QueryType.UPDATE.name -> {
                    UpdateGenerator(tableElement)
                }
                queryType.toString() == QueryType.INSERT.name -> {
                    InsertGenerator(tableElement)
                }
                queryType.toString() == QueryType.DELETE.name -> {
                    DeleteGenerator(tableElement)
                }
                else -> {
                    throw RuntimeException("Type $queryType not supported !")
                }
            }.execute(query, removeQuotes(attributes), removeQuotes(filters))
        }

        private fun removeQuotes(original: List<*>?): List<String>? {
            return original?.map { it.toString().replace("\"", "") }
        }
    }

}