package com.github.gr3gdev.jdbc.test

import com.github.gr3gdev.jdbc.JDBC
import com.github.gr3gdev.jdbc.JdbcConf
import com.github.gr3gdev.jdbc.test.jdbc.JDBCFactory

@JDBC([
    JdbcConf("/datasource1.properties", "test1"),
    JdbcConf("/datasource2.properties", "test2")
])
object DAORepository {

    fun init() {
        JDBCFactory.init()
    }

}