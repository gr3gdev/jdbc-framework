package com.github.gr3gdev.jdbc.test.dao

import com.github.gr3gdev.jdbc.dao.Queries
import com.github.gr3gdev.jdbc.dao.Query
import com.github.gr3gdev.jdbc.test.bean.Car

@Queries
interface CarDAO {

    @Query(sql = "INSERT Car (Car)")
    fun add(car: Car)

}