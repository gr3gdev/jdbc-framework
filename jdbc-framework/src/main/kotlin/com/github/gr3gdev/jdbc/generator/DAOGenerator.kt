package com.github.gr3gdev.jdbc.generator

import com.github.gr3gdev.jdbc.dao.Queries
import com.github.gr3gdev.jdbc.dao.Query
import com.github.gr3gdev.jdbc.metadata.element.TableElement
import com.github.gr3gdev.jdbc.processor.ReflectUtils
import com.github.gr3gdev.jdbc.template.DAOImplTemplate
import com.github.gr3gdev.jdbc.template.JDBCFactoryTemplate
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
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
        val implementation = ReflectUtils.getAnnotationAttributeValue(annotation, "implementation")
        val parent = if (implementation != null && implementation.toString() != Processor::class.qualifiedName) {
            "extends $implementation"
        } else {
            "implements $element"
        }
        val tablesElement = tables.map {
            TableElement(processingEnv, it, it.asType().toString())
        }.toSet()
        if (element.kind == ElementKind.INTERFACE) {
            val fileName = "${element.simpleName}Impl"
            val queries = element.enclosedElements
                    .filter { ReflectUtils.isAnnotationPresent(it, Query::class) }
                    .map {
                        QueryGenerator.generate(tablesElement, it)
                    }
            val imports = queries.flatMapTo(HashSet()) {
                it.imports
            }.sorted().joinToString("\n") {
                "import $it;"
            }
            val methods = queries.joinToString("\n") {
                it.method
            }
            val classContent = DAOImplTemplate.generate(packageName, imports, parent, fileName, methods)
            processingEnv.filer.createSourceFile("$packageName.$fileName").openWriter().use {
                it.write(classContent)
            }
            return Pair(element, fileName)
        }
        return Pair(element, null)
    }

}