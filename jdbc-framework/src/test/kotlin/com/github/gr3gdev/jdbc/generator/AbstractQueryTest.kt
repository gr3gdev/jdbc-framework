package com.github.gr3gdev.jdbc.generator

import com.github.gr3gdev.jdbc.AbstractTest
import com.github.gr3gdev.jdbc.ColumnTest
import com.github.gr3gdev.jdbc.generator.element.MethodElement
import com.github.gr3gdev.jdbc.metadata.element.TableElement
import org.junit.Before
import org.mockito.Mockito
import javax.lang.model.element.Name
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

internal abstract class AbstractQueryTest : AbstractTest() {

    lateinit var tables: Set<TableElement>

    val imports = setOf(
            "java.sql.Statement",
            "java.sql.SQLException",
            "java.sql.Connection",
            "java.sql.PreparedStatement",
            "java.sql.ResultSet"
    )

    val personClass = "com.github.gr3gdev.jdbc.test.Person"
    val addressClass = "com.github.gr3gdev.jdbc.test.Address"
    val townClass = "com.github.gr3gdev.jdbc.test.Town"
    private val petClass = "com.github.gr3gdev.jdbc.test.Pet"
    private val personPetClass = "com.github.gr3gdev.jdbc.test.PersonPet"

    private val columnsTown = listOf(
            ColumnTest("id", Int::class.java.canonicalName, true),
            ColumnTest("name", String::class.java.canonicalName)
    )
    private val columnsAddress = listOf(
            ColumnTest("id", Int::class.java.canonicalName , true),
            ColumnTest("street", String::class.java.canonicalName),
            ColumnTest("town", Int::class.java.canonicalName, false, columnsTown, "Town")
    )
    private val columnsPerson = listOf(
            ColumnTest("id", Long::class.java.canonicalName, true),
            ColumnTest("firstname", String::class.java.canonicalName),
            ColumnTest("lastname", String::class.java.canonicalName),
            ColumnTest("address", Int::class.java.canonicalName, false, columnsAddress, "Address")
    )
    private val columnsPet = listOf(
            ColumnTest("id", Int::class.java.canonicalName, true),
            ColumnTest("name", String::class.java.canonicalName),
            ColumnTest("type", String::class.java.canonicalName)
    )
    private val columnsPersonPet = listOf(
            ColumnTest("person", Long::class.java.canonicalName, true, columnsPerson, "Person"),
            ColumnTest("pet", Long::class.java.canonicalName, true, columnsPet, "Pet")
    )

    class Parameter(val name: String, val type: String)

    @Before
    fun init() {
        val tablePerson = mockTable("Person", columnsPerson, "MyDB")
        val tableAddress = mockTable("Address", columnsAddress, "MyDB")
        val tableTown = mockTable("Town", columnsTown, "MyDB")
        val tablePet = mockTable("Pet", columnsPet, "MyDB")
        val tablePersonPet = mockTable("PersonPet", columnsPersonPet, "MyDB")
        tables = linkedSetOf(
                TableElement(processingEnvironment, tablePerson, personClass),
                TableElement(processingEnvironment, tableAddress, addressClass),
                TableElement(processingEnvironment, tableTown, townClass),
                TableElement(processingEnvironment, tablePet, petClass),
                TableElement(processingEnvironment, tablePersonPet, personPetClass)
        )
    }

    fun mockMethod(name: String, returnType: String, parameters: List<Parameter>): MethodElement {
        val returnTypeMirror = Mockito.mock(TypeMirror::class.java)
        Mockito.`when`(returnTypeMirror.toString()).thenReturn(returnType)

        return MethodElement(name, returnTypeMirror, parameters.map {
            val variable = Mockito.mock(VariableElement::class.java)
            val variableName = Mockito.mock(Name::class.java)
            Mockito.`when`(variable.simpleName).thenReturn(variableName)
            Mockito.`when`(variableName.toString()).thenReturn(it.name)
            val variableType = Mockito.mock(TypeMirror::class.java)
            Mockito.`when`(variable.asType()).thenReturn(variableType)
            Mockito.`when`(variableType.toString()).thenReturn(it.type)
            variable
        })
    }
}