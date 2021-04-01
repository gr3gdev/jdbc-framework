package com.github.gr3gdev.jdbc.test.dao

import com.github.gr3gdev.jdbc.dao.Queries
import com.github.gr3gdev.jdbc.dao.Query
import com.github.gr3gdev.jdbc.test.bean.Town

@Queries
interface TownDAO {

    @Query(sql = "INSERT Town (Town.name)")
    fun add(town: Town)

}