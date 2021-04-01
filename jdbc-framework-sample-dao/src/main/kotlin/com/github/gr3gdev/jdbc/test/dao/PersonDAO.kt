package com.github.gr3gdev.jdbc.test.dao

import com.github.gr3gdev.jdbc.dao.Queries
import com.github.gr3gdev.jdbc.dao.Query
import com.github.gr3gdev.jdbc.test.bean.Person
import java.util.*

@Queries
interface PersonDAO {

    @Query(sql = "SELECT Person FROM Person")
    fun select(): List<Person>

    @Query(sql = "SELECT Person.id, Person.firstname, Person.personAddress FROM Person")
    fun selectAddress(): List<Person>

    @Query(sql = "SELECT Person.id, Person.firstname FROM Person WHERE Person.firstname")
    fun selectByName(firstname: String): Optional<Person>

    @Query(sql = "UPDATE Person SET Person.firstname WHERE Person.id")
    fun update(person: Person): Int

    @Query(sql = "INSERT Person (Person)")
    fun add(person: Person)

    @Query(sql = "DELETE Person WHERE Person.id")
    fun delete(person: Person): Int

}