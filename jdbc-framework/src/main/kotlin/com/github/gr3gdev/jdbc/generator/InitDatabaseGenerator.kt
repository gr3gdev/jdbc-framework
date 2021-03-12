package com.github.gr3gdev.jdbc.generator

import com.github.gr3gdev.jdbc.generator.element.GetterStructure
import com.github.gr3gdev.jdbc.generator.element.SortedTableElement
import com.github.gr3gdev.jdbc.generator.element.TableElement
import com.github.gr3gdev.jdbc.generator.impl.CreateTableGenerator
import com.github.gr3gdev.jdbc.metadata.Table
import com.github.gr3gdev.jdbc.processor.JDBCProcessor
import com.github.gr3gdev.jdbc.processor.ReflectUtils
import com.github.gr3gdev.jdbc.template.CreateTableTemplate
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element

object InitDatabaseGenerator {

    fun generate(processingEnv: ProcessingEnvironment, confAnnotation: AnnotationMirror, tables: Set<Element>, packageName: String, autoincrementSyntax: String): String {
        val tab = JDBCProcessor.TAB
        val configFile = ReflectUtils.getAnnotationAttributeValue(confAnnotation, "configFile")
        val databaseName = ReflectUtils.getAnnotationAttributeValue(confAnnotation, "databaseName") ?: "default"
        var index = 1
        val tableDatabase = tables.filter {
            val tableAnnotation = ReflectUtils.getAnnotation(it, Table::class)
            val tableDatabaseName = ReflectUtils.getAnnotationAttributeValue(tableAnnotation, "databaseName")
                    ?: "default"
            databaseName == tableDatabaseName
        }.map {
            val tableElement = TableElement(processingEnv, it, null)
            SortedTableElement(index++, it, tableElement)
        }
        var ordered = false
        while (!ordered) {
            ordered = true
            tableDatabase.forEach { meta ->
                if (tableDatabase.any { other ->
                            other.tableElement.name != meta.tableElement.name
                                    && other.order <= meta.order
                                    && other.tableElement.columns.any { c -> c.foreignKey?.name == meta.tableElement.name }
                        }) {
                    meta.order--
                    ordered = false
                }
            }
        }
        val classes = tableDatabase.sortedBy {
            it.order
        }.map { sortedTable ->
            val table = sortedTable.table
            val tableElement = sortedTable.tableElement
            val mainStructure = GetterStructure(null, tableElement, null)
            val generation = CreateTableGenerator(tableElement, autoincrementSyntax).execute(table, mainStructure, mainStructure)
            val imports = generation.first.joinToString("\n") { "import $it;" }
            val methods = generation.second
            val className = "${databaseName.toString().capitalize()}${table.simpleName.toString().capitalize()}"
            processingEnv.filer.createSourceFile("$packageName.${databaseName.toString().capitalize()}${table.simpleName.toString().capitalize()}")
                    .openWriter().use {
                        it.write(CreateTableTemplate.generate(packageName, imports, className, methods))
                    }
            "new $packageName.$className().create();"
        }
        return """SQLDataSource.init("$configFile", "$databaseName");
        ${classes.joinToString("\n$tab$tab")}
        """
    }

}