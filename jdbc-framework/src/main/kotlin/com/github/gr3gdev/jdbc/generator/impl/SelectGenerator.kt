package com.github.gr3gdev.jdbc.generator.impl

import com.github.gr3gdev.jdbc.generator.element.TableElement
import com.github.gr3gdev.jdbc.processor.JDBCProcessor
import com.github.gr3gdev.jdbc.generator.QueryGenerator
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement

internal class SelectGenerator(private val tableElement: TableElement) : QueryGenerator(tableElement) {

    override fun execute(element: Element, attributes: List<String>?, filters: List<String>?): Pair<List<String>, String> {
        (element as ExecutableElement)
        val tab = JDBCProcessor.TAB
        val returnType = element.returnType
        val selector = attributes?.joinToString(", ") {
            tableElement.getColumn(it).name()
        } ?: "*"
        var sql = "SELECT $selector FROM ${tableElement.name}"
        if (!filters.isNullOrEmpty()) {
            sql += " WHERE ${getFilters(filters)}"
        }
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
                "new $returnType()"
            }
        }
        var obj = "ret"
        var beforeMapping = ""
        var afterMapping = ""
        if (collection) {
            obj = "elt"
            beforeMapping = "final ${tableElement.classType!!} elt = new ${tableElement.classType}();\n$tab$tab$tab$tab$tab"
            afterMapping = "\n$tab$tab$tab$tab${tab}ret.add(elt);"
        }
        val mapAttributes = attributes?.joinToString("\n$tab$tab$tab$tab$tab") {
            getResult(it, obj)
        } ?: tableElement.columns.joinToString("\n$tab$tab$tab$tab$tab") {
            if (it.foreignKey != null) {
                """final ${it.type} ${it.fieldName} = new ${it.type}();
                    ${getResult(it.foreignKey!!.fieldName, it.fieldName, it.name())}
                    $obj.set${it.fieldName.capitalize()}(${it.fieldName});"""
            } else {
                getResult(it.fieldName, obj)
            }
        }
        val content = """
    @Override
    public $returnType ${element.simpleName}(${joinParameters(element)}) {
        final $returnType ret = $instance;
        final String sql = "$sql";
        try (${getConnection()};
            final PreparedStatement stm = cnx.prepareStatement(sql)) {
            ${setParameters(filters, element) ?: "// Without conditions"}
            try (final ResultSet res = stm.executeQuery()) {
                while (res.next()) {
                    $beforeMapping$mapAttributes$afterMapping
                }
                return ret;
            }
        } catch (SQLException throwables) {
            ${throwException("com.github.gr3gdev.jdbc.dao.QueryType.SELECT", element.parameters)}
        }
    }
        """
        return Pair(imports(), content)
    }

}