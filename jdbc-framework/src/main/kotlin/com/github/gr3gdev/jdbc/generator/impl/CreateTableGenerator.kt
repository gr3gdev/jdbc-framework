package com.github.gr3gdev.jdbc.generator.impl

import com.github.gr3gdev.jdbc.generator.QueryGenerator
import com.github.gr3gdev.jdbc.generator.element.GetterStructure
import com.github.gr3gdev.jdbc.generator.element.TableElement
import com.github.gr3gdev.jdbc.processor.JDBCProcessor
import javax.lang.model.element.Element

internal class CreateTableGenerator(private val tableElement: TableElement, private val autoincrementSyntax: String) : QueryGenerator(tableElement) {

    override fun execute(element: Element, attributes: GetterStructure, filters: GetterStructure): Pair<List<String>, String> {
        val tab = JDBCProcessor.TAB
        var columns = tableElement.columns.map {
            val nullable = if (it.primaryKey || it.required) {
                " NOT NULL"
            } else {
                " NULL"
            }
            val autoincrement = if (it.autoincrement) {
                " $autoincrementSyntax"
            } else {
                ""
            }
            if (it.foreignKey != null) {
                ".append(\"${it.foreignKey!!.getPrimaryKey().name()}_${it.foreignKey!!.name} ${it.sqlType}$autoincrement$nullable\")"
            } else {
                ".append(\"${it.name()} ${it.sqlType}$autoincrement$nullable\")"
            }
        }
        val primaryKeys = tableElement.columns.filter {
            it.primaryKey
        }
        if (primaryKeys.isNotEmpty()) {
            columns = columns.plus(".append(\"PRIMARY KEY (${primaryKeys.joinToString(", ") { it.name() }})\")")
        }
        val foreignKeys = tableElement.columns.filter {
            it.foreignKey != null
        }

        foreignKeys.forEach {
            val fkTableName = it.name()
            val fkColumnName = it.foreignKey!!.getPrimaryKey().name()
            val columnName = "${fkColumnName}_${it.name()}"
            val tableName = it.tableName
            columns = columns.plus(".append(\"CONSTRAINT FK_${tableName}_${fkTableName} FOREIGN KEY (${columnName}) REFERENCES $fkTableName (${fkColumnName})\")")
        }
        val content = """
    /**
     * Method for create TABLE : ${tableElement.name}.
     */
    public void create() {
        final StringBuilder sql = new StringBuilder("CREATE TABLE ${tableElement.name} (")
            ${columns.joinToString("\n$tab$tab${tab}.append(\",\")\n$tab$tab$tab")}
            .append(")");
        SQLDataSource.execute("${tableElement.databaseName}", sql.toString(), (stm) -> {
            stm.executeUpdate();
        });
    }
        """
        return Pair(imports(), content)
    }

}