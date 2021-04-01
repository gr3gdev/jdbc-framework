package com.github.gr3gdev.jdbc.generator

import com.github.gr3gdev.jdbc.error.JDBCConfigurationException
import com.github.gr3gdev.jdbc.generator.element.QueryElement
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class QueryDeleteTest : AbstractQueryTest() {

    @Test
    fun `test delete return type`() {
        val query = QueryElement("DELETE Person",
                mockMethod("delete",
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
    fun `test delete all Person`() {
        val query = QueryElement("DELETE Person",
                mockMethod("deleteAll",
                        "int",
                        emptyList()))
        val generated = query.generate(tables)
        assertEquals(imports, generated.imports)
        assertEquals(readFile("testDeleteAll"), generated.method)
    }

    @Test
    fun `test Person by lastname`() {
        val query = QueryElement("DELETE Person WHERE Person.lastname",
                mockMethod("deleteByLastname",
                        "int",
                        listOf(Parameter("lastname", "java.lang.String"))))
        val generated = query.generate(tables)
        assertEquals(imports, generated.imports)
        assertEquals(readFile("testDeleteByLastname"), generated.method)
    }

}