package com.github.gr3gdev.jdbc.error

import com.github.gr3gdev.jdbc.dao.QueryType
import java.sql.SQLException

/**
 * Exception when an error is occured at a query execution.
 */
class JDBCExecutionException(val type: QueryType, parameters: String, cause: SQLException) :
        RuntimeException("Error on ${type.name} with parameters ($parameters)", cause)
