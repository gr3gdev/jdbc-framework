package com.github.gr3gdev.jdbc.test.dao

import com.github.gr3gdev.jdbc.dao.Queries
import com.github.gr3gdev.jdbc.dao.Query
import com.github.gr3gdev.jdbc.dao.QueryType
import com.github.gr3gdev.jdbc.test.bean.PersonAddress

@Queries(PersonAddress::class)
interface PersonAddressDAO {

    @Query(QueryType.INSERT, ["street", "town"])
    fun add(personAddress: PersonAddress)

}