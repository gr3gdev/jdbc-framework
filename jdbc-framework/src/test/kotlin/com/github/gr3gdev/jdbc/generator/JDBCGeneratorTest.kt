package com.github.gr3gdev.jdbc.generator

import com.github.gr3gdev.jdbc.AbstractTest
import com.github.gr3gdev.jdbc.ColumnTest
import com.github.gr3gdev.jdbc.JDBC
import com.github.gr3gdev.jdbc.dao.Queries
import com.github.gr3gdev.jdbc.dao.Query
import com.github.gr3gdev.jdbc.dao.QueryType
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
                ColumnTest("id", Long::class.java.canonicalName, null),
                ColumnTest("name", String::class.java.canonicalName, null)
        )
        val table = mockTable("MyObject", columns, "db1")
        Mockito.`when`(table.toString()).thenReturn(type)
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
        val attributes = listOf("name", "id")
        val filters = listOf("id")
        val methods = listOf(
                createMethod("add", QueryType.INSERT, null, null, listOf(objectParameter)),
                createMethod("update", QueryType.UPDATE, attributes, null, listOf(idParameter, nameParameter)),
                createMethod("find", QueryType.SELECT, null, null, emptyList()),
                createMethod("delete", QueryType.DELETE, null, filters, emptyList())
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

    private fun createMethod(name: String, type: QueryType, attributes: List<Any>?, filters: List<Any>?, parameters: List<VariableElement>): ExecutableElement {
        val methodElement = Mockito.mock(ExecutableElement::class.java)
        val methodName = Mockito.mock(Name::class.java)
        Mockito.`when`(methodName.toString()).thenReturn(name)
        Mockito.`when`(methodElement.simpleName).thenReturn(methodName)
        val methodAnnotation = mockAnnotation(methodElement, Query::class)
        mockAnnotationAttributes(methodAnnotation, mapOf(
                "type" to type,
                "attributes" to attributes,
                "filters" to filters
        ))
        val returnType = Mockito.mock(TypeMirror::class.java)
        if (QueryType.UPDATE == type || QueryType.DELETE == type) {
            Mockito.`when`(returnType.toString()).thenReturn("int")
        } else if (QueryType.SELECT == type) {
            Mockito.`when`(returnType.toString()).thenReturn("java.util.Optional<com.github.gr3gdev.jdbc.test.MyObject>")
        }
        Mockito.`when`(methodElement.parameters).thenReturn(parameters)
        Mockito.`when`(methodElement.returnType).thenReturn(returnType)
        return methodElement
    }

    @Test
    fun testProcessInitJDBC() {
        JDBCGenerator.processInitJDBC(processingEnvironment, element, tables, elementsDAO)
        assertEquals("""
package com.github.gr3gdev.jdbc.test.jdbc;

import com.github.gr3gdev.jdbc.SQLDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Auto-generated class.
 */
class MyObjectDAOImpl implements com.github.gr3gdev.jdbc.test.dao.MyObjectDAO {
    
    @Override
    public void add(final com.github.gr3gdev.jdbc.test.MyObject obj) {
        final String sql = "INSERT INTO MY_OBJECT (NAME) VALUES (?)";
        SQLDataSource.executeAndGetKey("db1", sql, (stm) -> {
            stm.setString(1, obj.getName());
            stm.executeUpdate();
        }, (res) -> {
            obj.setId(res.getLong(1));
        });
    }
            

    @Override
    public int update(final long id, final java.lang.String name) {
        final String sql = "UPDATE MY_OBJECT SET ID = ?, NAME = ?";
        return SQLDataSource.executeAndUpdate("db1", sql, (stm) -> {
            stm.setLong(1, id);
            stm.setString(2, name);
            return stm.executeUpdate();
        });
    }
        

    @Override
    public java.util.Optional<com.github.gr3gdev.jdbc.test.MyObject> find() {
        final String sql = "SELECT my_object_1.ID, my_object_1.NAME FROM MY_OBJECT my_object_1";
        return SQLDataSource.executeAndReturn("db1", sql, (stm) -> {
            // Without conditions
        }, (res) -> {
            final java.util.Optional<com.github.gr3gdev.jdbc.test.MyObject> ret;
            if (res.next()) {
                final com.github.gr3gdev.jdbc.test.MyObject elt = new com.github.gr3gdev.jdbc.test.MyObject();
                elt.setId(res.getLong("ID"));
                elt.setName(res.getString("NAME"));
                ret = java.util.Optional.of(elt);
                return ret;
            }
            return java.util.Optional.empty();
        });
    }
        

    @Override
    public int delete() {
        final String sql = "DELETE FROM MY_OBJECT WHERE ID = ?";
        return SQLDataSource.executeAndUpdate("db1", sql, (stm) -> {
            stm.setLong(1, null);
            return stm.executeUpdate();
        });
    }
        
}
""".trimIndent(), daoWriter.buffer.toString())

        assertEquals("""
package com.github.gr3gdev.jdbc.test.jdbc;

import com.github.gr3gdev.jdbc.SQLDataSource;

import com.github.gr3gdev.jdbc.test.dao.MyObjectDAO;

/**
 * Auto-generated class.
 */
public class JDBCFactory {

    /**
     * Init the database(s).
     */
    public static void init() {
        SQLDataSource.init("/path/to/file1.properties", "db1");
    }

    // DAO implementations
    
    /**
     * Get an instance of {@link com.github.gr3gdev.jdbc.test.dao.MyObjectDAO}.
     *
     * @return {@link com.github.gr3gdev.jdbc.test.dao.MyObjectDAO}
     */
    public static MyObjectDAO getMyObjectDAO() {
        return new MyObjectDAOImpl();
    }
    // end DAO implementations

}
""".trimIndent(), jdbcWriter.buffer.toString())
    }

}
