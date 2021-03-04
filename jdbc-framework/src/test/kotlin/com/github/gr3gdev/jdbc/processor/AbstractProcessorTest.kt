package com.github.gr3gdev.jdbc.processor

import com.github.gr3gdev.jdbc.generator.element.TableElement
import com.github.gr3gdev.jdbc.metadata.Column
import com.github.gr3gdev.jdbc.metadata.Table
import org.junit.Before
import org.mockito.Mock
import org.mockito.Mockito
import java.math.BigDecimal
import java.net.URL
import java.sql.Time
import java.sql.Timestamp
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Types

abstract class AbstractProcessorTest {

    companion object {
        val ID = Pair("id", Long::class.java)
        val NAME = Pair("name", String::class.java)
        val DATE_CREATION = Pair("dateCreation", java.sql.Date::class.java)

        val columns = listOf(
                ID, NAME, DATE_CREATION,
                Pair("dateBirthday", Date::class.java),
                Pair("average", Float::class.java),
                Pair("order", Int::class.java),
                Pair("active", Boolean::class.java),
                Pair("deleted", Byte::class.java),
                Pair("double", Double::class.java),
                Pair("timeModified", Timestamp::class.java),
                Pair("url", URL::class.java),
                Pair("number", BigDecimal::class.java),
                Pair("short", Short::class.java),
                Pair("time", Time::class.java)
        )
    }

    @Mock
    lateinit var processingEnvironment: ProcessingEnvironment

    @Mock
    lateinit var typeUtils: Types

    @Mock
    lateinit var table: Element

    @Mock
    lateinit var tableAnnotation: AnnotationMirror

    @Mock
    lateinit var tableAnnotationType: DeclaredType

    @Mock
    lateinit var tableAnnotationElementValues: MutableMap<ExecutableElement, AnnotationValue>

    @Mock
    lateinit var name: Name

    @Mock
    lateinit var query: ExecutableElement

    @Mock
    lateinit var queryReturnType: TypeMirror

    @Mock
    lateinit var queryName: Name

    @Mock
    lateinit var testParameter: VariableElement

    @Mock
    lateinit var testParameterName: Name

    @Mock
    lateinit var testParameterType: TypeMirror

    internal lateinit var tableElement: TableElement

    @Before
    fun init() {
        Mockito.`when`(processingEnvironment.typeUtils).thenReturn(typeUtils)

        val columnsElement = columns.map { column ->
            val columnElement = Mockito.mock(Element::class.java)
            val columnAnnotation = Mockito.mock(AnnotationMirror::class.java)
            val columnAnnotationType = Mockito.mock(DeclaredType::class.java)
            val columnType = Mockito.mock(TypeMirror::class.java)
            val columnName = Mockito.mock(Name::class.java)

            Mockito.`when`(typeUtils.asElement(columnType)).thenReturn(columnElement)
            Mockito.`when`(columnAnnotationType.toString()).thenReturn(Column::class.qualifiedName)
            Mockito.`when`(columnAnnotation.annotationType).thenReturn(columnAnnotationType)
            Mockito.`when`(columnElement.annotationMirrors).thenReturn(listOf(columnAnnotation))
            val pkElement = Mockito.mock(ExecutableElement::class.java)
            val pkElementName = Mockito.mock(Name::class.java)
            Mockito.`when`(pkElement.simpleName).thenReturn(pkElementName)
            Mockito.`when`(pkElementName.toString()).thenReturn("primaryKey")
            val pkValue = Mockito.mock(AnnotationValue::class.java)
            Mockito.`when`(pkValue.value).thenReturn(column.first == "id")
            val columnAnnotationValues = Pair(pkElement, pkValue)
            Mockito.`when`(columnAnnotation.elementValues).thenReturn(hashMapOf(columnAnnotationValues))
            Mockito.`when`(columnElement.simpleName).thenReturn(columnName)
            Mockito.`when`(columnName.toString()).thenReturn(column.first)
            Mockito.`when`(columnElement.asType()).thenReturn(columnType)
            Mockito.`when`(columnType.toString()).thenReturn(column.second.canonicalName)

            columnElement
        }

        val databaseNameElement = Mockito.mock(ExecutableElement::class.java)
        val databaseNameElementName = Mockito.mock(Name::class.java)
        Mockito.`when`(databaseNameElementName.toString()).thenReturn("databaseName")
        Mockito.`when`(databaseNameElement.simpleName).thenReturn(databaseNameElementName)
        val databaseNameValue = Mockito.mock(AnnotationValue::class.java)
        Mockito.`when`(databaseNameValue.value).thenReturn("TestDB")
        val entryDatabaseNameAnnotation = object : MutableMap.MutableEntry<ExecutableElement, AnnotationValue> {
            override val key: ExecutableElement
                get() = databaseNameElement
            override val value: AnnotationValue
                get() = databaseNameValue

            override fun setValue(newValue: AnnotationValue): AnnotationValue = databaseNameValue
        }

        Mockito.`when`(table.simpleName).thenReturn(name)
        Mockito.`when`(name.toString()).thenReturn("Test")
        Mockito.`when`(table.enclosedElements).thenReturn(columnsElement)
        Mockito.`when`(table.annotationMirrors).thenReturn(listOf(tableAnnotation))
        Mockito.`when`(tableAnnotation.annotationType).thenReturn(tableAnnotationType)
        Mockito.`when`(tableAnnotation.elementValues).thenReturn(tableAnnotationElementValues)
        Mockito.`when`(tableAnnotationElementValues.entries).thenReturn(hashSetOf(entryDatabaseNameAnnotation))
        Mockito.`when`(tableAnnotationType.toString()).thenReturn(Table::class.qualifiedName)

        Mockito.`when`(query.simpleName).thenReturn(queryName)
        Mockito.`when`(query.parameters).thenReturn(listOf(testParameter))

        Mockito.`when`(testParameter.simpleName).thenReturn(testParameterName)
        Mockito.`when`(testParameterName.toString()).thenReturn("test")
        Mockito.`when`(testParameter.asType()).thenReturn(testParameterType)
        Mockito.`when`(testParameterType.toString()).thenReturn("com.github.gr3gdev.jdbc.Test")

        tableElement = TableElement(processingEnvironment, table, "com.github.gr3gdev.jdbc.Test")
    }

}