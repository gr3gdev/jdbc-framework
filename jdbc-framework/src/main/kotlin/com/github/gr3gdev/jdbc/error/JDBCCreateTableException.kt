package com.github.gr3gdev.jdbc.error

import java.sql.SQLException

/**
 * Exception when an error is occured at the creation table.
 */
class JDBCCreateTableException(message: String, cause: SQLException) : RuntimeException(message, cause)
