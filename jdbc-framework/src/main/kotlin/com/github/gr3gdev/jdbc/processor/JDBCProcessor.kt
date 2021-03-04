package com.github.gr3gdev.jdbc.processor

import com.github.gr3gdev.jdbc.JDBC
import com.github.gr3gdev.jdbc.dao.Queries
import com.github.gr3gdev.jdbc.generator.JDBCGenerator
import com.github.gr3gdev.jdbc.metadata.Table
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

class JDBCProcessor : AbstractProcessor() {

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(JDBC::class.java.name,
                Table::class.java.name,
                Queries::class.java.name)
    }

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        try {
            // Generate TABLES init
            val tables = roundEnv.getElementsAnnotatedWith(Table::class.java)
            val jdbc = roundEnv.getElementsAnnotatedWith(JDBC::class.java)
            if (jdbc.size > 1) {
                throw RuntimeException("Only one @JDBC is authorized !")
            }
            jdbc.forEach {
                JDBCGenerator.processInitJDBC(processingEnv, it, tables, roundEnv.getElementsAnnotatedWith(Queries::class.java))
            }
        } catch (exc: Exception) {
            exc.printStackTrace()
            throw RuntimeException(exc)
        }
        return true
    }

    companion object {
        const val TAB = "    "
    }

}