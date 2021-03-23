package com.github.gr3gdev.jdbc.generator.impl

import com.github.gr3gdev.jdbc.error.JDBCConfigurationException
import com.github.gr3gdev.jdbc.generator.QueryGenerator
import com.github.gr3gdev.jdbc.generator.element.GetterStructure
import com.github.gr3gdev.jdbc.generator.element.TableElement
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement

internal class DeleteGenerator(private val tableElement: TableElement) : QueryGenerator(tableElement) {

    override fun execute(element: Element, attributes: GetterStructure, filters: GetterStructure): Pair<List<String>, String> {
        (element as ExecutableElement)
        if (filters.children.isEmpty()) {
            throw JDBCConfigurationException("Filters must be defined for DELETE")
        }
        if (element.returnType.toString() != "int") {
            throw JDBCConfigurationException("Query must be return int")
        }
        val sql = "DELETE FROM ${tableElement.name} WHERE ${getFilters(null, filters)}"
        val content = """
    @Override
    public int ${element.simpleName}(${joinParameters(element)}) {
        final String sql = "$sql";
        return SQLDataSource.executeAndUpdate("${tableElement.databaseName}", sql, (stm) -> {
            ${setParameters(filters.children, element)}
            return stm.executeUpdate();
        });
    }
        """
        return Pair(imports(), content)
    }

}