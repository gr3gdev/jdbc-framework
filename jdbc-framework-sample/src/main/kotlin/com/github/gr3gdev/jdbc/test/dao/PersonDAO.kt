package com.github.gr3gdev.jdbc.test.dao

import com.github.gr3gdev.jdbc.dao.Queries
import com.github.gr3gdev.jdbc.dao.Query
import com.github.gr3gdev.jdbc.dao.QueryType
import com.github.gr3gdev.jdbc.test.bean.Person

@Queries(Person::class)
interface PersonDAO {

    @Query(QueryType.SELECT)
    fun select(): List<Person>

    @Query(QueryType.SELECT, attributes = ["id", "name"], filters = ["name"])
    fun selectByName(name: String): Person

    @Query(QueryType.UPDATE, attributes = ["name"], filters = ["id"])
    fun update(person: Person): Int

    @Query(QueryType.INSERT, attributes = ["name", "personAddress", "car"])
    fun add(person: Person)

    @Query(QueryType.DELETE, filters = ["id"])
    fun delete(person: Person): Int

}