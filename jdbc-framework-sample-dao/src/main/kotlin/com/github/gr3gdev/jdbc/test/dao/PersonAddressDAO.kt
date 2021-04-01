package com.github.gr3gdev.jdbc.test.dao

import com.github.gr3gdev.jdbc.dao.Queries
import com.github.gr3gdev.jdbc.dao.Query
import com.github.gr3gdev.jdbc.test.bean.PersonAddress

@Queries
interface PersonAddressDAO {

    @Query(sql = "INSERT PersonAddress (PersonAddress)")
    fun add(personAddress: PersonAddress)

}