package com.github.gr3gdev.jdbc.generator.impl

import com.github.gr3gdev.jdbc.error.JDBCConfigurationException
import com.github.gr3gdev.jdbc.generator.QueryGenerator
import com.github.gr3gdev.jdbc.generator.element.TableElement
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement

internal class UpdateGenerator(private val tableElement: TableElement) : QueryGenerator(tableElement) {

    override fun execute(element: Element, attributes: List<String>?, filters: List<String>?): Pair<List<String>, String> {
        (element as ExecutableElement)
        attributes ?: throw JDBCConfigurationException("Attributes must be defined for UPDATE")
        if (element.returnType.toString() != "int") {
            throw JDBCConfigurationException("Query must be return int")
        }
        var sql = "UPDATE ${tableElement.name} SET ${
            attributes.joinToString(", ") {
                "${tableElement.getColumn(it).name()} = ?"
            }
        }"
        if (!filters.isNullOrEmpty()) {
            sql += " WHERE ${getFilters(filters)}"
        }
        var params = listOf<String>().plus(attributes)
        filters?.forEach { params = params.plus(it) }
        val content = """
    @Override
    public int ${element.simpleName}(${joinParameters(element)}) {
        final String sql = "$sql";
        try (${getConnection()};
            final PreparedStatement stm = cnx.prepareStatement(sql)) {
            ${setParameters(params, element)}
            return stm.executeUpdate();
        } catch (SQLException throwables) {
            ${throwException("com.github.gr3gdev.jdbc.dao.QueryType.UPDATE", element.parameters)}
        }
    }
        """
        return Pair(imports(), content)
    }

}