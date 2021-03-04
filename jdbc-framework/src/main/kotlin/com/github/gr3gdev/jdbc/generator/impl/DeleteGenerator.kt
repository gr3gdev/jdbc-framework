package com.github.gr3gdev.jdbc.generator.impl

import com.github.gr3gdev.jdbc.error.JDBCConfigurationException
import com.github.gr3gdev.jdbc.generator.QueryGenerator
import com.github.gr3gdev.jdbc.generator.element.TableElement
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement

internal class DeleteGenerator(private val tableElement: TableElement) : QueryGenerator(tableElement) {

    override fun execute(element: Element, attributes: List<String>?, filters: List<String>?): Pair<List<String>, String> {
        (element as ExecutableElement)
        filters ?: throw JDBCConfigurationException("Filters must be defined for DELETE")
        if (element.returnType.toString() != "int") {
            throw JDBCConfigurationException("Query must be return int")
        }
        val sql = "DELETE FROM ${tableElement.name} WHERE ${getFilters(filters)}"
        val content = """
    @Override
    public int ${element.simpleName}(${joinParameters(element)}) {
        final String sql = "$sql";
        try (${getConnection()};
            final PreparedStatement stm = cnx.prepareStatement(sql)) {
            ${setParameters(filters, element)}
            return stm.executeUpdate();
        } catch (SQLException throwables) {
            ${throwException("com.github.gr3gdev.jdbc.dao.QueryType.DELETE", element.parameters)}
        }
    }
        """
        return Pair(imports(), content)
    }

}