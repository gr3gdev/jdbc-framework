package com.github.gr3gdev.jdbc.generator

import com.github.gr3gdev.jdbc.error.JDBCConfigurationException
import com.github.gr3gdev.jdbc.generator.element.QueryElement
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class QueryUpdateTest : AbstractQueryTest() {

    @Test
    fun `test update return type`() {
        val query = QueryElement("UPDATE Person SET Person",
                mockMethod("update",
                        "void",
                        emptyList()))
        try {
            query.generate(tables)
            fail()
        } catch (exc: JDBCConfigurationException) {
            assertEquals("Query must be return int", exc.message)
        }
    }

    @Test
    fun `test update all Person`() {
        val query = QueryElement("UPDATE Person SET Person.firstname",
                mockMethod("updateAll",
                        "int",
                        listOf(Parameter("firstname", "java.lang.String"))))
        val generated = query.generate(tables)
        assertEquals(imports, generated.imports)
        assertEquals(readFile("testUpdateAll"), generated.method)
    }

    @Test
    fun `test update Person by id`() {
        val query = QueryElement("UPDATE Person SET Person WHERE Person.id",
                mockMethod("updateByPersonId",
                        "int",
                        listOf(Parameter("id", "java.lang.Long"),
                                Parameter("person", personClass))))
        val generated = query.generate(tables)
        assertEquals(imports, generated.imports)
        assertEquals(readFile("testUpdateByPersonId"), generated.method)
    }

}