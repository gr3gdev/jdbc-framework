package com.github.gr3gdev.jdbc.generator

import com.github.gr3gdev.jdbc.error.JDBCConfigurationException
import com.github.gr3gdev.jdbc.generator.element.QueryElement
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class QueryInsertTest : AbstractQueryTest() {

    @Test
    fun `test insert return type`() {
        val query = QueryElement("INSERT Person (Person.firstname, Person.lastname)",
                mockMethod("insert",
                        "int",
                        emptyList()))
        try {
            query.generate(tables)
            Assert.fail()
        } catch (exc: JDBCConfigurationException) {
            Assert.assertEquals("Query must be void method", exc.message)
        }
    }

    @Test
    fun `test insert Person`() {
        val query = QueryElement("INSERT Person (Person.firstname, Person.lastname)",
                mockMethod("insert",
                        "void",
                        listOf(Parameter("firstname", "java.lang.String"),
                                Parameter("lastname", "java.lang.String"))))
        val generated = query.generate(tables)
        Assert.assertEquals(imports, generated.imports)
        Assert.assertEquals(readFile("testInsertPerson"), generated.method)
    }

    @Test
    fun `test insert Person with object`() {
        val query = QueryElement("INSERT Person (Person)",
                mockMethod("insert",
                        "void",
                        listOf(Parameter("person", personClass))))
        val generated = query.generate(tables)
        Assert.assertEquals(imports, generated.imports)
        Assert.assertEquals(readFile("testInsertPersonWithObject"), generated.method)
    }

    @Test
    fun `test insert Address mode batch`() {
        val query = QueryElement("INSERT Address (Address)",
                mockMethod("insertAddresses",
                        "void",
                        listOf(Parameter("addresses", "java.util.List<$addressClass>"))))
        val generated = query.generate(tables)
        Assert.assertEquals(imports, generated.imports)
        Assert.assertEquals(readFile("testInsertAddresses"), generated.method)
    }

}