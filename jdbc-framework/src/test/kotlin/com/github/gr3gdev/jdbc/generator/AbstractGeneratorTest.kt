package com.github.gr3gdev.jdbc.generator

import com.github.gr3gdev.jdbc.AbstractTest
import com.github.gr3gdev.jdbc.ColumnTest
import com.github.gr3gdev.jdbc.dao.Query
import com.github.gr3gdev.jdbc.dao.QueryType
import org.junit.Before
import org.mockito.Mock
import org.mockito.Mockito
import java.util.*
import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror
import kotlin.collections.ArrayList

abstract class AbstractGeneratorTest : AbstractTest() {

    @Mock
    lateinit var query: ExecutableElement

    lateinit var table: Element

    private val townColumns = listOf(
            ColumnTest("id", Int::class.java.canonicalName, null),
            ColumnTest("name", String::class.java.canonicalName, null)
    )

    private val addressColumns = listOf(
            ColumnTest("id", Long::class.java.canonicalName, null),
            ColumnTest("street", String::class.java.canonicalName, null),
            ColumnTest("town", "com.github.gr3gdev.jdbc.test.Town", townColumns)
    )

    private val tableColumns = listOf(
            ColumnTest("id", Long::class.java.canonicalName, null),
            ColumnTest("firstname", String::class.java.canonicalName, null),
            ColumnTest("lastname", String::class.java.canonicalName, null),
            ColumnTest("birthdate", Date::class.java.canonicalName, null),
            ColumnTest("address", "com.github.gr3gdev.jdbc.test.Address", addressColumns)
    )

    @Before
    fun init() {
        table = mockTable("person", tableColumns, "TEST_DB")
    }

    class Parameter(val className: String, val name: String)

    protected fun initQuery(queryType: QueryType, methodName: String, returnType: String?,
                            parameters: List<Parameter>?,
                            attributes: List<String>?, filters: List<String>?, joins: List<AnnotationMirror>? = null) {
        val queryName = Mockito.mock(Name::class.java)

        val queryAnnotation = mockAnnotation(query, Query::class)
        mockAnnotationAttributes(queryAnnotation, mapOf(
                "type" to queryType,
                "attributes" to attributes,
                "filters" to filters,
                "joins" to joins
        ))
        if (returnType != null) {
            val returnValue = Mockito.mock(TypeMirror::class.java)
            Mockito.`when`(returnValue.toString()).thenReturn(returnType)
            Mockito.`when`(query.returnType).thenReturn(returnValue)
        }
        Mockito.`when`(queryName.toString()).thenReturn(methodName)
        Mockito.`when`(query.simpleName).thenReturn(queryName)
        val queryParameters = ArrayList<VariableElement>()
        parameters?.forEach { parameter ->
            val queryParameter = mockElement(parameter.name, parameter.className, VariableElement::class.java)
            queryParameters.add(queryParameter)
        }
        Mockito.`when`(query.parameters).thenReturn(queryParameters)

    }

}