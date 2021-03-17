package com.github.gr3gdev.jdbc.processor

import com.github.gr3gdev.jdbc.JDBC
import com.github.gr3gdev.jdbc.dao.Queries
import com.github.gr3gdev.jdbc.generator.JDBCGenerator
import com.github.gr3gdev.jdbc.metadata.Table
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@SupportedAnnotationTypes(value = [
    "com.github.gr3gdev.jdbc.JDBC",
    "com.github.gr3gdev.jdbc.metadata.Table",
    "com.github.gr3gdev.jdbc.dao.Queries"])
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class JDBCProcessor : AbstractProcessor() {

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
            throw exc
        }
        return true
    }

    companion object {
        const val TAB = "    "
    }

}