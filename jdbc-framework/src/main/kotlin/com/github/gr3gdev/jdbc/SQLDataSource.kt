package com.github.gr3gdev.jdbc

import com.github.gr3gdev.jdbc.error.JDBCExecutionException
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.*

object SQLDataSource {

    private lateinit var config: HikariConfig
    private val dataSources = HashMap<String, HikariDataSource>()

    interface Execution<T, R> {
        @Throws(SQLException::class)
        fun run(obj: T): R
    }

    interface StatementExecution {
        @Throws(SQLException::class)
        fun run(stm: PreparedStatement)
    }

    interface ResultSetExecution {
        @Throws(SQLException::class)
        fun run(res: ResultSet)
    }

    @JvmStatic
    fun init(configFile: String, databaseName: String) {
        config = HikariConfig(configFile)
        dataSources[databaseName] = HikariDataSource(config)
    }

    @JvmStatic
    fun <R> executeAndUpdate(databaseName: String, sql: String, func: Execution<PreparedStatement, R>): R {
        getConnection(databaseName).use { cnx ->
            cnx.prepareStatement(sql).use { stm ->
                try {
                    return func.run(stm)
                } catch (exc: SQLException) {
                    throw JDBCExecutionException(sql, exc)
                }
            }
        }
    }

    @JvmStatic
    fun <R> executeAndReturn(databaseName: String, sql: String, func: StatementExecution, get: Execution<ResultSet, R>): R {
        getConnection(databaseName).use { cnx ->
            cnx.prepareStatement(sql).use { stm ->
                try {
                    func.run(stm)
                    stm.executeQuery().use { res ->
                        return get.run(res)
                    }
                } catch (exc: SQLException) {
                    throw JDBCExecutionException(sql, exc)
                }
            }
        }
    }

    @JvmStatic
    fun execute(databaseName: String, sql: String, func: StatementExecution) {
        getConnection(databaseName).use { cnx ->
            cnx.prepareStatement(sql).use { stm ->
                try {
                    func.run(stm)
                } catch (exc: SQLException) {
                    throw JDBCExecutionException(sql, exc)
                }
            }
        }
    }

    @JvmStatic
    fun executeAndGetKey(databaseName: String, sql: String,
                         func: StatementExecution,
                         getKey: ResultSetExecution) {
        getConnection(databaseName).use { cnx ->
            cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { stm ->
                try {
                    func.run(stm)
                    stm.generatedKeys.use { res ->
                        if (res.next()) {
                            getKey.run(res)
                        }
                    }
                } catch (exc: SQLException) {
                    throw JDBCExecutionException(sql, exc)
                }
            }
        }
    }

    private fun getConnection(databaseName: String): Connection = dataSources[databaseName]?.connection ?: throw RuntimeException("Database '$databaseName' not found")

}