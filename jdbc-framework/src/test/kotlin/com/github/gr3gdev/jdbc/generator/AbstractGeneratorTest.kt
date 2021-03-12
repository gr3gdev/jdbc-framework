package com.github.gr3gdev.jdbc.generator

import com.github.gr3gdev.jdbc.dao.Query
import com.github.gr3gdev.jdbc.dao.QueryType
import com.github.gr3gdev.jdbc.metadata.Column
import com.github.gr3gdev.jdbc.metadata.Table
import org.junit.Before
import org.mockito.Mock
import org.mockito.Mockito
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Types
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.reflect.KClass

abstract class AbstractGeneratorTest {

    @Mock
    protected lateinit var processingEnvironment: ProcessingEnvironment

    @Mock
    private lateinit var typeUtils: Types

    @Mock
    lateinit var query: ExecutableElement

    lateinit var table: Element

    internal class ColumnTest(val name: String, val clazz: String?, val sqlType: String, val columns: List<ColumnTest>?)

    private val townColumns = listOf(
            ColumnTest("id", Int::class.java.canonicalName, "INT", null),
            ColumnTest("name", String::class.java.canonicalName, "VARCHAR(100)", null)
    )

    private val addressColumns = listOf(
            ColumnTest("id", Long::class.java.canonicalName, "LONG", null),
            ColumnTest("street", String::class.java.canonicalName, "TEXT", null),
            ColumnTest("town", "com.github.gr3gdev.jdbc.test.Town", "INT", townColumns)
    )

    private val tableColumns = listOf(
            ColumnTest("id", Long::class.java.canonicalName, "LONG", null),
            ColumnTest("firstname", String::class.java.canonicalName, "VARCHAR(20)", null),
            ColumnTest("lastname", String::class.java.canonicalName, "VARCHAR(40)", null),
            ColumnTest("birthdate", Date::class.java.canonicalName, "DATE", null),
            ColumnTest("address", "com.github.gr3gdev.jdbc.test.Address", "LONG", addressColumns)
    )

    @Before
    fun init() {
        Mockito.`when`(processingEnvironment.typeUtils).thenReturn(typeUtils)

        table = mockTable("person", tableColumns)
    }

    private fun mockTable(tableName: String, tableColumns: List<ColumnTest>): Element {
        val tableElement = Mockito.mock(Element::class.java)
        val tableSimpleName = Mockito.mock(Name::class.java)
        val tableType = Mockito.mock(TypeMirror::class.java)
        Mockito.`when`(tableElement.asType()).thenReturn(tableType)
        Mockito.`when`(tableType.toString()).thenReturn("com.github.gr3gdev.jdbc.test.${tableName.capitalize()}")
        Mockito.`when`(tableElement.simpleName).thenReturn(tableSimpleName)
        Mockito.`when`(tableSimpleName.toString()).thenReturn(tableName.capitalize())
        val columns = tableColumns.map {
            val element = Mockito.mock(Element::class.java)
            val elementName = Mockito.mock(Name::class.java)
            val elementType = Mockito.mock(TypeMirror::class.java)
            Mockito.`when`(elementName.toString()).thenReturn(it.name)
            Mockito.`when`(element.simpleName).thenReturn(elementName)
            Mockito.`when`(elementType.toString()).thenReturn(it.clazz)
            Mockito.`when`(element.asType()).thenReturn(elementType)
            if (it.columns != null) {
                val fkTable = mockTable(it.name, it.columns)
                Mockito.`when`(typeUtils.asElement(elementType)).thenReturn(fkTable)
            } else {
                Mockito.`when`(typeUtils.asElement(elementType)).thenReturn(element)
            }
            val columnAnnotation = mockAnnotation(element, Column::class)
            mockAnnotationAttributes(columnAnnotation, mapOf(
                    "primaryKey" to (it.name == "id"),
                    "autoincrement" to (it.name == "id"),
                    "required" to true,
                    "sqlType" to it.sqlType
            ))
            element
        }
        Mockito.`when`(tableElement.enclosedElements).thenReturn(columns)

        val tableAnnotation = mockAnnotation(tableElement, Table::class)
        mockAnnotationAttributes(tableAnnotation, mapOf(
                "databaseName" to "TEST_DB"
        ))
        return tableElement
    }

    private fun mockAnnotation(element: Element, annotationClass: KClass<*>): AnnotationMirror {
        val annotation = Mockito.mock(AnnotationMirror::class.java)
        val annotationType = Mockito.mock(DeclaredType::class.java)
        Mockito.`when`(element.annotationMirrors).thenReturn(listOf(annotation))
        Mockito.`when`(annotation.annotationType).thenReturn(annotationType)
        Mockito.`when`(annotationType.toString()).thenReturn(annotationClass.qualifiedName)
        return annotation
    }

    private fun mockAnnotationAttributes(annotation: AnnotationMirror, attributes: Map<String, Any?>) {
        val entries = HashMap<ExecutableElement, AnnotationValue>()
        attributes.forEach { (name, value) ->
            val attributeElement = Mockito.mock(ExecutableElement::class.java)
            val attributeElementName = Mockito.mock(Name::class.java)
            Mockito.`when`(attributeElement.simpleName).thenReturn(attributeElementName)
            Mockito.`when`(attributeElementName.toString()).thenReturn(name)
            val attributeValue = Mockito.mock(AnnotationValue::class.java)
            Mockito.`when`(attributeValue.value).thenReturn(value)
            entries[attributeElement] = attributeValue
        }
        Mockito.`when`(annotation.elementValues).thenReturn(entries)
    }

    class Parameter(val className: String, val name: String)

    protected fun initQuery(queryType: QueryType, methodName: String, returnType: String?,
                            parameters: List<Parameter>?,
                            attributes: List<String>?, filters: List<String>?) {
        val queryName = Mockito.mock(Name::class.java)

        val queryAnnotation = mockAnnotation(query, Query::class)
        mockAnnotationAttributes(queryAnnotation, mapOf(
                "type" to queryType,
                "attributes" to attributes,
                "filters" to filters
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
            val queryParameter = Mockito.mock(VariableElement::class.java)
            val queryParameterName = Mockito.mock(Name::class.java)
            val queryParameterType = Mockito.mock(TypeMirror::class.java)
            Mockito.`when`(queryParameterName.toString()).thenReturn(parameter.name)
            Mockito.`when`(queryParameter.simpleName).thenReturn(queryParameterName)
            Mockito.`when`(queryParameterType.toString()).thenReturn(parameter.className)
            Mockito.`when`(queryParameter.asType()).thenReturn(queryParameterType)
            queryParameters.add(queryParameter)
        }
        Mockito.`when`(query.parameters).thenReturn(queryParameters)

    }

}