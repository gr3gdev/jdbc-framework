package com.github.gr3gdev.jdbc.generator.impl

import com.github.gr3gdev.jdbc.error.JDBCConfigurationException
import com.github.gr3gdev.jdbc.generator.QueryGenerator
import com.github.gr3gdev.jdbc.generator.element.ColumnElement
import com.github.gr3gdev.jdbc.generator.element.GetterStructure
import com.github.gr3gdev.jdbc.generator.element.TableElement
import com.github.gr3gdev.jdbc.processor.JDBCProcessor
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.TypeMirror

internal class SelectGenerator(private val tableElement: TableElement) : QueryGenerator(tableElement) {

    override fun execute(element: Element, attributes: GetterStructure, filters: GetterStructure): Pair<List<String>, String> {
        (element as ExecutableElement)
        val returnType = element.returnType
        val sql = getSQL(attributes, filters)
        val pair = getNewInstance(returnType)
        val instance = pair.first
        val collection = pair.second
        if (!collection && !returnType.toString().startsWith("java.util.Optional")) {
            throw JDBCConfigurationException("Select an unique object must return Optional<${tableElement.classType}>")
        }
        val mapAttributes = getMappingAttributes(collection, attributes)
        val parseResult = if (collection) {
            "while"
        } else {
            "if"
        }
        val content = if (collection) {
            """
    @Override
    public $returnType ${element.simpleName}(${joinParameters(element)}) {
        final $returnType ret = $instance;
        final String sql = "$sql";
        return SQLDataSource.executeAndReturn("${tableElement.databaseName}", sql, (stm) -> {
            ${setParameters(filters.children, element) ?: "// Without conditions"}
        }, (res) -> {
            $parseResult (res.next()) {
                $mapAttributes
            }
            return ret;
        });
    }
        """
        } else {
            """
    @Override
    public $returnType ${element.simpleName}(${joinParameters(element)}) {
        final String sql = "$sql";
        return SQLDataSource.executeAndReturn("${tableElement.databaseName}", sql, (stm) -> {
            ${setParameters(filters.children, element) ?: "// Without conditions"}
        }, (res) -> {
            final $returnType ret;
            $parseResult (res.next()) {
                $mapAttributes
                return ret;
            }
            return java.util.Optional.empty();
        });
    }
        """
        }
        return Pair(imports(), content)
    }

    private fun getMappingAttributes(collection: Boolean, attributes: GetterStructure): String {
        val tab = JDBCProcessor.TAB
        val obj = "elt"
        val beforeMapping: String
        val afterMapping: String
        beforeMapping = "final ${tableElement.classType!!} elt = new ${tableElement.classType}();\n$tab$tab$tab$tab"
        afterMapping = if (collection) {
            "\n$tab$tab$tab${tab}ret.add(elt);"
        } else {
            "\n$tab$tab$tab${tab}ret = java.util.Optional.of(elt);"
        }
        val result = ArrayList<String>()
        result(result, obj, attributes)
        val mapAttributes = result.joinToString("\n$tab$tab$tab$tab")
        return "$beforeMapping$mapAttributes$afterMapping"
    }

    private fun getColumnResult(table: TableElement, it: ColumnElement, obj: String): String {
        return if (it.foreignKey != null) {
            """final ${it.type} ${it.fieldName} = new ${it.type}();
                ${getResult(table, it.foreignKey!!.getPrimaryKey().fieldName, it.fieldName, it.name())}
                $obj.set${it.fieldName.capitalize()}(${it.fieldName});"""
        } else {
            getResult(table, it.fieldName, obj)
        }
    }

    private fun result(res: ArrayList<String>, obj: String, structure: GetterStructure) {
        structure.children.filter { it.column != null }
                .forEach {
                    res.add(getColumnResult(structure.table, it.column!!, obj))
                    result(res, it.column.fieldName, it)
                }
    }

    private fun getNewInstance(returnType: TypeMirror): Pair<String, Boolean> {
        var collection = false
        val instance = when {
            returnType.toString().startsWith("java.util.List") -> {
                collection = true
                "new java.util.ArrayList()"
            }
            returnType.toString().startsWith("java.util.Set") -> {
                collection = true
                "new java.util.HashSet()"
            }
            else -> {
                "java.util.Optional.empty()"
            }
        }
        return Pair(instance, collection)
    }

    private fun getSQL(attributes: GetterStructure, filters: GetterStructure): String {
        val alias = attributes.alias
        val selector = ArrayList<String>()
        select(selector, attributes)
        var sql = "SELECT ${selector.joinToString(", ")} FROM ${tableElement.name} $alias"
        sql += join(attributes)
        if (!filters.children.isNullOrEmpty()) {
            sql += " WHERE ${getFilters(alias, filters)}"
        }
        return sql
    }

    private fun select(sql: ArrayList<String>, structure: GetterStructure) {
        val alias = structure.alias
        structure.children.forEach {
            if (it.children.isNotEmpty()) {
                select(sql, it)
            }
            if (it.column?.foreignKey != null) {
                sql.add("$alias.${it.column.foreignKey!!.getPrimaryKey().name()}_${it.column.name()}")
            } else {
                sql.add("$alias.${it.column?.name()}")
            }
        }
    }

    private fun join(structure: GetterStructure): String {
        return structure.children.filter {
            it.children.isNotEmpty()
        }.joinToString(" ") {
            " ${it.joinType} JOIN ${it.table.name} ${it.alias} ON ${it.alias}.${it.table.getPrimaryKey().name()} = ${it.parent?.alias}.${it.table.getPrimaryKey().name()}_${it.column?.name()}" + join(it)
        }
    }

}