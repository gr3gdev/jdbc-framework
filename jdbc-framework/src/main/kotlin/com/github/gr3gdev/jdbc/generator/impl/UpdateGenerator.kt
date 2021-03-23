package com.github.gr3gdev.jdbc.generator.impl

import com.github.gr3gdev.jdbc.error.JDBCConfigurationException
import com.github.gr3gdev.jdbc.generator.QueryGenerator
import com.github.gr3gdev.jdbc.generator.element.GetterStructure
import com.github.gr3gdev.jdbc.generator.element.TableElement
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement

internal class UpdateGenerator(private val tableElement: TableElement) : QueryGenerator(tableElement) {

    override fun execute(element: Element, attributes: GetterStructure, filters: GetterStructure): Pair<List<String>, String> {
        (element as ExecutableElement)
        if (attributes.children.isEmpty()) {
            throw JDBCConfigurationException("Attributes must be defined for UPDATE")
        }
        if (element.returnType.toString() != "int") {
            throw JDBCConfigurationException("Query must be return int")
        }
        var sql = "UPDATE ${tableElement.name} SET ${
            attributes.children
                    .filter { it.column != null }
                    .joinToString(", ") {
                        val column = it.column!!
                        if (column.foreignKey != null) {
                            "${column.foreignKey!!.getPrimaryKey().name()}_${column.foreignKey!!.name} = ?"
                        } else {
                            "${column.name()} = ?"
                        }
                    }
        }"
        if (!filters.children.isNullOrEmpty()) {
            sql += " WHERE ${getFilters(null, filters)}"
        }
        val content = """
    @Override
    public int ${element.simpleName}(${joinParameters(element)}) {
        final String sql = "$sql";
        return SQLDataSource.executeAndUpdate("${tableElement.databaseName}", sql, (stm) -> {
            ${setParameters(attributes.children.plus(filters.children), element)}
            return stm.executeUpdate();
        });
    }
        """
        return Pair(imports(), content)
    }

}