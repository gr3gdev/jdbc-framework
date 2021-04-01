package com.github.gr3gdev.jdbc.generator

import com.github.gr3gdev.jdbc.AbstractTest
import com.github.gr3gdev.jdbc.ColumnTest
import com.github.gr3gdev.jdbc.JDBC
import com.github.gr3gdev.jdbc.dao.Queries
import com.github.gr3gdev.jdbc.dao.Query
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.io.StringWriter
import javax.annotation.processing.Filer
import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.tools.JavaFileObject

@RunWith(MockitoJUnitRunner::class)
class JDBCGeneratorTest : AbstractTest() {

    @Mock
    private lateinit var element: Element

    @Mock
    private lateinit var filer: Filer

    private lateinit var tables: HashSet<Element>
    private lateinit var elementsDAO: MutableSet<Element>

    private lateinit var daoWriter: StringWriter
    private lateinit var jdbcWriter: StringWriter

    @Before
    fun `init @JDBC`() {
        daoWriter = StringWriter()
        jdbcWriter = StringWriter()

        val type = "com.github.gr3gdev.jdbc.test.MyObject"

        val packageElement = Mockito.mock(PackageElement::class.java)
        val elementUtils = Mockito.mock(Elements::class.java)
        Mockito.`when`(packageElement.toString()).thenReturn("com.github.gr3gdev.jdbc.test")
        Mockito.`when`(elementUtils.getPackageOf(element)).thenReturn(packageElement)
        Mockito.`when`(processingEnvironment.elementUtils).thenReturn(elementUtils)

        Mockito.`when`(processingEnvironment.filer).thenReturn(filer)
        val daoSourceFile = Mockito.mock(JavaFileObject::class.java)
        Mockito.`when`(daoSourceFile.openWriter()).thenReturn(daoWriter)
        val jdbcSourceFile = Mockito.mock(JavaFileObject::class.java)
        Mockito.`when`(jdbcSourceFile.openWriter()).thenReturn(jdbcWriter)
        Mockito.`when`(filer.createSourceFile(Mockito.eq("com.github.gr3gdev.jdbc.test.jdbc.MyObjectDAOImpl"))).thenReturn(daoSourceFile)
        Mockito.`when`(filer.createSourceFile(Mockito.eq("com.github.gr3gdev.jdbc.test.jdbc.JDBCFactory"))).thenReturn(jdbcSourceFile)

        tables = HashSet()
        val columns = listOf(
                ColumnTest("id", Long::class.java.canonicalName, true),
                ColumnTest("name", String::class.java.canonicalName)
        )
        val table = mockTable("MyObject", columns, "db1")
        tables.add(table)

        elementsDAO = HashSet()
        val tableDAO = mockElement("MyObjectDAO", "com.github.gr3gdev.jdbc.test.dao.MyObjectDAO", Element::class.java)
        Mockito.`when`(tableDAO.toString()).thenReturn("com.github.gr3gdev.jdbc.test.dao.MyObjectDAO")
        Mockito.`when`(tableDAO.kind).thenReturn(ElementKind.INTERFACE)
        val packageDaoElement = Mockito.mock(PackageElement::class.java)
        Mockito.`when`(packageDaoElement.toString()).thenReturn("com.github.gr3gdev.jdbc.test.dao")
        Mockito.`when`(elementUtils.getPackageOf(tableDAO)).thenReturn(packageDaoElement)
        val objectParameter = mockParameter("obj", type)
        val idParameter = mockParameter("id", Long::class.java.canonicalName)
        val nameParameter = mockParameter("name", String::class.java.canonicalName)
        val methods = listOf(
                createMethod("add", "insert MyObject (MyObject)", "void", listOf(objectParameter)),
                createMethod("update", "Update MyObject Set MyObject.name Where MyObject.id", "int", listOf(idParameter, nameParameter)),
                createMethod("find", "SELECT MyObject FROM MyObject", "java.util.List<$type>", emptyList()),
                createMethod("delete", "DELETE MyObject WHERE MyObject.id", "int", listOf(idParameter))
        )
        Mockito.`when`(tableDAO.enclosedElements).thenReturn(methods)
        val tableDAOAnnotation = mockAnnotation(tableDAO, Queries::class)
        mockAnnotationAttributes(tableDAOAnnotation, mapOf(
                "mapTo" to type
        ))
        elementsDAO.add(tableDAO)

        val jdbcAnnotation = mockAnnotation(element, JDBC::class)
        val confAnnotation = Mockito.mock(AnnotationMirror::class.java)
        mockAnnotationAttributes(confAnnotation, mapOf(
                "configFile" to "/path/to/file1.properties",
                "databaseName" to "db1"
        ))
        mockAnnotationAttributes(jdbcAnnotation, mapOf(
                "conf" to listOf(confAnnotation)
        ))
    }

    private fun mockParameter(name: String, type: String): VariableElement {
        val objectParameter = Mockito.mock(VariableElement::class.java)
        val parameterType = Mockito.mock(TypeMirror::class.java)
        val parameterName = Mockito.mock(Name::class.java)
        Mockito.`when`(parameterName.toString()).thenReturn(name)
        Mockito.`when`(objectParameter.simpleName).thenReturn(parameterName)
        Mockito.`when`(parameterType.toString()).thenReturn(type)
        Mockito.`when`(objectParameter.asType()).thenReturn(parameterType)
        return objectParameter
    }

    private fun createMethod(name: String, sql: String, returnTypeString: String, parameters: List<VariableElement>): ExecutableElement {
        val methodElement = Mockito.mock(ExecutableElement::class.java)
        val methodName = Mockito.mock(Name::class.java)
        Mockito.`when`(methodName.toString()).thenReturn(name)
        Mockito.`when`(methodElement.simpleName).thenReturn(methodName)
        val methodAnnotation = mockAnnotation(methodElement, Query::class)
        mockAnnotationAttributes(methodAnnotation, mapOf(
                "sql" to sql
        ))
        val returnType = Mockito.mock(TypeMirror::class.java)
        Mockito.`when`(returnType.toString()).thenReturn(returnTypeString)
        Mockito.`when`(methodElement.parameters).thenReturn(parameters)
        Mockito.`when`(methodElement.returnType).thenReturn(returnType)
        return methodElement
    }

    @Test
    fun testProcessInitJDBC() {
        JDBCGenerator.processInitJDBC(processingEnvironment, element, tables, elementsDAO)
        assertEquals(readFile("testDAOGenerator"), daoWriter.buffer.toString())
        assertEquals(readFile("testJDBCFactory"), jdbcWriter.buffer.toString())
    }

}
