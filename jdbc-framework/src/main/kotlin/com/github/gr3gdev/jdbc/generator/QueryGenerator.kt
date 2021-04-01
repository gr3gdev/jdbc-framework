package com.github.gr3gdev.jdbc.generator

import com.github.gr3gdev.jdbc.dao.Query
import com.github.gr3gdev.jdbc.dao.generator.element.DaoMethodElement
import com.github.gr3gdev.jdbc.generator.element.MethodElement
import com.github.gr3gdev.jdbc.generator.element.QueryElement
import com.github.gr3gdev.jdbc.metadata.element.TableElement
import com.github.gr3gdev.jdbc.processor.ReflectUtils
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement

internal object QueryGenerator {

    fun generate(tables: Set<TableElement>, query: Element): DaoMethodElement {
        val queryAnnotation = ReflectUtils.getAnnotation(query, Query::class)
        val sqlValue = ReflectUtils.getAnnotationAttributeValue(queryAnnotation, "sql") as String?
                ?: throw RuntimeException("Sql request not found")
        (query as ExecutableElement)
        val queryElement = QueryElement(sqlValue, MethodElement(query.simpleName.toString(), query.returnType,
                query.parameters.sortedBy { it.simpleName.toString() }))
        return queryElement.generate(tables)
    }


}