package com.github.gr3gdev.jdbc.test.dao

import com.github.gr3gdev.jdbc.dao.Queries
import com.github.gr3gdev.jdbc.dao.Query
import com.github.gr3gdev.jdbc.dao.QueryType
import com.github.gr3gdev.jdbc.test.bean.Person
import java.util.*

@Queries(Person::class)
interface PersonDAO {

    @Query(QueryType.SELECT)
    fun select(): List<Person>

    @Query(QueryType.SELECT, attributes = ["id", "firstname", "personAddress.street", "personAddress.town.name"])
    fun selectAddress(): List<Person>

    @Query(QueryType.SELECT, attributes = ["id", "firstname"], filters = ["firstname"])
    fun selectByName(firstname: String): Optional<Person>

    @Query(QueryType.UPDATE, attributes = ["firstname"], filters = ["id"])
    fun update(person: Person): Int

    @Query(QueryType.INSERT, attributes = ["firstname", "personAddress", "car"])
    fun add(person: Person)

    @Query(QueryType.DELETE, filters = ["id"])
    fun delete(person: Person): Int

}