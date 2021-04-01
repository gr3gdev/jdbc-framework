package com.github.gr3gdev.jdbc.generator

import com.github.gr3gdev.jdbc.generator.element.QueryElement
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class QuerySelectTest : AbstractQueryTest() {

    @Test
    fun `test select all Person`() {
        val query = QueryElement("SELECT Person FROM Person",
                mockMethod("selectAll",
                        "java.util.List<$personClass>",
                        emptyList()))
        val generated = query.generate(tables)
        assertEquals(imports, generated.imports)
        assertEquals(readFile("testSelectAllPerson"), generated.method)
    }

    @Test
    fun `test select Person with condition`() {
        val query = QueryElement("SELECT Person.firstname, Person.lastname FROM Person WHERE Person.firstname",
                mockMethod("selectByFirstname",
                        "java.util.List<$personClass>",
                        listOf(Parameter("firstname", "java.lang.String"))))
        val generated = query.generate(tables)
        assertEquals(imports, generated.imports)
        assertEquals(readFile("testSelectByFirstname"), generated.method)
    }

    @Test
    fun `test select a Person Address`() {
        val query = QueryElement("SELECT Person.address FROM Person WHERE Person.id",
                mockMethod("selectAddressById",
                        "java.util.Optional<$addressClass>",
                        listOf(Parameter("id", "java.lang.Long"))))
        val generated = query.generate(tables)
        assertEquals(imports, generated.imports)
        assertEquals(readFile("testSelectAddressById"), generated.method)
    }

    @Test
    fun `test select Person by Pet name`() {
        val query = QueryElement("SELECT PersonPet.person FROM PersonPet WHERE PersonPet.pet.name",
                mockMethod("selectPersonByPetName",
                        "java.util.List<$personClass>",
                        listOf(Parameter("name", "java.lang.String"))))
        val generated = query.generate(tables)
        assertEquals(imports, generated.imports)
        assertEquals(readFile("testSelectPersonByPetName"), generated.method)
    }

    @Test
    fun `test select by object`() {
        val query = QueryElement("SELECT Town.name FROM Town WHERE Town.id",
                mockMethod("selectTown",
                        "java.util.Optional<$townClass>",
                        listOf(Parameter("town", townClass))))
        val generated = query.generate(tables)
        assertEquals(imports, generated.imports)
        assertEquals(readFile("testSelectTown"), generated.method)
    }

    @Test
    fun `test select Person by Town name`() {
        val query = QueryElement("SELECT Person FROM Person WHERE Person.address.town.name",
                mockMethod("selectPersonByTownName",
                        "java.util.List<$personClass>",
                        listOf(Parameter("name", "java.lang.String"))))
        val generated = query.generate(tables)
        assertEquals(imports, generated.imports)
        assertEquals(readFile("testSelectPersonByTownName"), generated.method)
    }

}