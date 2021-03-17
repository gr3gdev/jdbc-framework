package com.github.gr3gdev.jdbc.generator.impl

import com.github.gr3gdev.jdbc.error.JDBCConfigurationException
import com.github.gr3gdev.jdbc.generator.QueryGenerator
import com.github.gr3gdev.jdbc.generator.element.GetterStructure
import com.github.gr3gdev.jdbc.generator.element.TableElement
import com.github.gr3gdev.jdbc.processor.JDBCProcessor
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement

internal class InsertGenerator(private val tableElement: TableElement) : QueryGenerator(tableElement) {

    override fun execute(element: Element, attributes: GetterStructure, filters: GetterStructure): Pair<List<String>, String> {
        (element as ExecutableElement)
        val tab = JDBCProcessor.TAB
        var insertAttributes = attributes.children.filter { it.column != null }
                .map { it.column!!.fieldName }
        if (insertAttributes.isEmpty()) {
            insertAttributes = tableElement.columns
                    .filter { !it.autoincrement }
                    .map { it.fieldName }
        }
        if (element.parameters.isEmpty()) {
            throw JDBCConfigurationException("Insert must have one parameter !")
        }
        if (element.parameters.size > 1) {
            throw JDBCConfigurationException("Insert must have only one parameter !")
        }
        val parameter = element.parameters[0]
        val isBatch = isCollectionParameter(parameter)
        val sql = "INSERT INTO ${tableElement.name} (${
            insertAttributes.joinToString(", ") {
                val col = tableElement.getColumn(it)
                if (col.foreignKey != null) {
                    "${col.foreignKey!!.getPrimaryKey().name()}_${col.name()}"
                } else {
                    col.name()
                }
            }
        }) VALUES (${
            insertAttributes.joinToString(", ") { "?" }
        })"
        val setID = tableElement.columns.filter { it.primaryKey }.joinToString("\n$tab$tab$tab$tab") {
            "${parameter.simpleName}.set${it.fieldName.capitalize()}(res.get${mappingTypes[it.type.toString()]}(\"${it.name()}\"));"
        }
        val execution = if (isBatch) {
            """SQLDataSource.execute("${tableElement.databaseName}", sql, (stm) -> {
            int index = 0;
            for (final ${tableElement.classType} element : ${parameter.simpleName}) {
                ${setParameters("element", insertAttributes, "\n$tab$tab$tab$tab")}
                stm.addBatch();
                index++;
                if (index % 1000 == 0 || index == ${parameter.simpleName}.size()) {
                    stm.executeBatch();
                }
            }
        });"""
        } else {
            """SQLDataSource.executeAndGetKey("${tableElement.databaseName}", sql, (stm) -> {
            ${setParameters(parameter.simpleName.toString(), insertAttributes, "\n$tab$tab$tab")}
            stm.executeUpdate();
        }, (res) -> {
            $setID
        });"""
        }
        val content = """
    @Override
    public void ${element.simpleName}(${joinParameters(element)}) {
        final String sql = "$sql";
        $execution
    }
            """
        return Pair(imports("java.sql.Statement"), content)
    }

}