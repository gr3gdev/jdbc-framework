package com.github.gr3gdev.jdbc.test.dao

import com.github.gr3gdev.jdbc.dao.Queries
import com.github.gr3gdev.jdbc.dao.Query
import com.github.gr3gdev.jdbc.dao.QueryType
import com.github.gr3gdev.jdbc.test.bean.Town

@Queries(Town::class)
interface TownDAO {

    @Query(QueryType.INSERT, ["name"])
    fun add(town: Town)

}