package com.github.gr3gdev.jdbc.test.dao

import com.github.gr3gdev.jdbc.dao.Queries
import com.github.gr3gdev.jdbc.dao.Query
import com.github.gr3gdev.jdbc.dao.QueryType
import com.github.gr3gdev.jdbc.test.bean.Car

@Queries(Car::class)
interface CarDAO {

    @Query(QueryType.INSERT, ["label", "clean"])
    fun add(car: Car)

}