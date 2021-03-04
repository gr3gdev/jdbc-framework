package com.github.gr3gdev.jdbc.generator

import com.github.gr3gdev.jdbc.dao.Queries
import com.github.gr3gdev.jdbc.dao.Query
import com.github.gr3gdev.jdbc.processor.ReflectUtils
import com.github.gr3gdev.jdbc.template.DAOImplTemplate
import com.github.gr3gdev.jdbc.template.JDBCFactoryTemplate
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind

internal object DAOGenerator {

    fun generate(processingEnv: ProcessingEnvironment, elementsDAO: MutableSet<out Element>, packageName: String, tables: Set<Element>): Pair<List<Pair<Element, String?>>, String> {
        val classes = elementsDAO.map { dao ->
            processImplementDAO(processingEnv, dao, packageName, tables)
        }
        return Pair(classes, classes.joinToString("\n") {
            JDBCFactoryTemplate.getter(it)
        })
    }

    private fun processImplementDAO(processingEnv: ProcessingEnvironment, element: Element, packageName: String, tables: Set<Element>): Pair<Element, String?> {
        val annotation = ReflectUtils.getAnnotation(element, Queries::class)
        val type = ReflectUtils.getAnnotationAttributeValue(annotation, "mapTo")
        val table = tables.firstOrNull { it.toString() == type.toString() }
                ?: throw RuntimeException("$type is not a table")
        if (element.kind == ElementKind.INTERFACE) {
            val fileName = "${element.simpleName}Impl"
            val queries = element.enclosedElements.map {
                when {
                    ReflectUtils.isAnnotationPresent(it, Query::class) -> {
                        QueryGenerator.generate(processingEnv, table, type, it)
                    }
                    else -> Pair(listOf(), "")
                }
            }
            val imports = queries.flatMapTo(HashSet()) {
                it.first
            }.sorted().joinToString("\n") {
                "import $it;"
            }
            val methods = queries.joinToString("\n") {
                it.second
            }
            val classContent = DAOImplTemplate.generate(packageName, imports, element, fileName, methods)
            processingEnv.filer.createSourceFile("$packageName.$fileName").openWriter().use { it.write(classContent) }
            return Pair(element, fileName)
        }
        return Pair(element, null)
    }

}