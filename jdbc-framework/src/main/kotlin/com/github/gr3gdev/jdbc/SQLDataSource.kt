package com.github.gr3gdev.jdbc

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

object SQLDataSource {

    private lateinit var config: HikariConfig
    private val dataSources = HashMap<String, HikariDataSource>()

    @JvmStatic
    fun init(configFile: String, databaseName: String) {
        config = HikariConfig(configFile)
        dataSources[databaseName] = HikariDataSource(config)
    }

    @JvmStatic
    fun getConnection(databaseName: String): Connection = dataSources[databaseName]?.connection ?: throw RuntimeException("Database '$databaseName' not found")

}