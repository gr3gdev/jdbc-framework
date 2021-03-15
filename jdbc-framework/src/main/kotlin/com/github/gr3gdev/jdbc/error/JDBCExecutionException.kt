package com.github.gr3gdev.jdbc.error

import java.sql.SQLException

/**
 * Exception when an error is occured at a query execution.
 */
class JDBCExecutionException(val sql: String, cause: SQLException) :
        RuntimeException("Error on $sql", cause)
