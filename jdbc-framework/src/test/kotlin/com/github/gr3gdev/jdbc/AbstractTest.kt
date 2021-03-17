package com.github.gr3gdev.jdbc

import com.github.gr3gdev.jdbc.metadata.Column
import com.github.gr3gdev.jdbc.metadata.Table
import org.junit.Before
import org.mockito.Mock
import org.mockito.Mockito
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Types
import kotlin.reflect.KClass

abstract class AbstractTest {

    @Mock
    protected lateinit var processingEnvironment: ProcessingEnvironment

    @Mock
    private lateinit var typeUtils: Types

    @Before
    fun `init ProcessingEnvironment`() {
        Mockito.`when`(processingEnvironment.typeUtils).thenReturn(typeUtils)
    }

    protected fun mockTable(tableName: String, tableColumns: List<ColumnTest>, databaseName: String): Element {
        val tableElement = mockElement(tableName.capitalize(), "com.github.gr3gdev.jdbc.test.${tableName.capitalize()}", Element::class.java)
        val columns = tableColumns.map {
            val type = if (it.columns != null) {
                mockTable(it.name, it.columns, databaseName)
            } else {
                null
            }
            val element = mockElement(it.name, it.clazz, Element::class.java, type)
            val columnAnnotation = mockAnnotation(element, Column::class)
            mockAnnotationAttributes(columnAnnotation, mapOf(
                    "primaryKey" to (it.name == "id"),
                    "autoincrement" to (it.name == "id")
            ))
            element
        }
        Mockito.`when`(tableElement.enclosedElements).thenReturn(columns)

        val tableAnnotation = mockAnnotation(tableElement, Table::class)
        mockAnnotationAttributes(tableAnnotation, mapOf(
                "databaseName" to databaseName
        ))
        return tableElement
    }

    protected fun <T : Element> mockElement(name: String, clazz: String?, typeElement: Class<T>, overrideType: Element? = null): T {
        val element = Mockito.mock(typeElement)
        val elementName = Mockito.mock(Name::class.java)
        val elementType = Mockito.mock(TypeMirror::class.java)
        Mockito.`when`(elementName.toString()).thenReturn(name)
        Mockito.`when`(element.simpleName).thenReturn(elementName)
        Mockito.`when`(elementType.toString()).thenReturn(clazz)
        Mockito.`when`(element.asType()).thenReturn(elementType)
        Mockito.`when`(typeUtils.asElement(elementType)).thenReturn(overrideType ?: element)
        return element
    }

    protected fun mockAnnotation(element: Element, annotationClass: KClass<*>): AnnotationMirror {
        val annotation = Mockito.mock(AnnotationMirror::class.java)
        val annotationType = Mockito.mock(DeclaredType::class.java)
        Mockito.`when`(element.annotationMirrors).thenReturn(listOf(annotation))
        Mockito.`when`(annotation.annotationType).thenReturn(annotationType)
        Mockito.`when`(annotationType.toString()).thenReturn(annotationClass.qualifiedName)
        return annotation
    }

    protected fun mockAnnotationAttributes(annotation: AnnotationMirror, attributes: Map<String, Any?>) {
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

}