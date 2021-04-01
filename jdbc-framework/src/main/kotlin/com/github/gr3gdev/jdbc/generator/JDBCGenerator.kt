package com.github.gr3gdev.jdbc.generator

import com.github.gr3gdev.jdbc.JDBC
import com.github.gr3gdev.jdbc.processor.JDBCProcessor
import com.github.gr3gdev.jdbc.processor.ReflectUtils
import com.github.gr3gdev.jdbc.template.JDBCFactoryTemplate
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element

internal object JDBCGenerator {

    fun processInitJDBC(processingEnv: ProcessingEnvironment, element: Element, tables: Set<Element>, elementsDAO: MutableSet<out Element>) {
        val tab = JDBCProcessor.TAB
        val annotation = ReflectUtils.getAnnotation(element, JDBC::class)
        val conf = ReflectUtils.getAnnotationAttributeValue(annotation, "conf") as List<*>?
        val packageName = "${processingEnv.elementUtils.getPackageOf(element)}.jdbc"
        val initMethods = conf?.joinToString("\n${tab.repeat(2)}") { confAnnotation ->
            InitDatabaseGenerator.generate(confAnnotation as AnnotationMirror)
        }
        val fileName = "JDBCFactory"
        // Generate DAO implementations
        val daoImplementations = DAOGenerator.generate(processingEnv, elementsDAO, packageName, tables)
        val imports = daoImplementations.first.joinToString("\n") {
            "import ${processingEnv.elementUtils.getPackageOf(it.first)}.${it.first.simpleName};"
        }
        val classContent = JDBCFactoryTemplate.generate(packageName, imports, fileName,
                initMethods, daoImplementations.second)
        processingEnv.filer.createSourceFile("$packageName.$fileName").openWriter().use { it.write(classContent) }
    }

}