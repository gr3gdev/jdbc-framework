package com.github.gr3gdev.jdbc.dao

import com.github.gr3gdev.jdbc.dao.generator.RequestGenerator
import com.github.gr3gdev.jdbc.dao.generator.impl.DeleteGenerator
import com.github.gr3gdev.jdbc.dao.generator.impl.InsertGenerator
import com.github.gr3gdev.jdbc.dao.generator.impl.SelectGenerator
import com.github.gr3gdev.jdbc.dao.generator.impl.UpdateGenerator
import com.github.gr3gdev.jdbc.dao.parser.RequestParser
import com.github.gr3gdev.jdbc.dao.parser.impl.DeleteParser
import com.github.gr3gdev.jdbc.dao.parser.impl.InsertParser
import com.github.gr3gdev.jdbc.dao.parser.impl.SelectParser
import com.github.gr3gdev.jdbc.dao.parser.impl.UpdateParser

internal enum class QueryType(
        val generator: RequestGenerator,
        val parser: RequestParser
) {

    SELECT(SelectGenerator(), SelectParser()),
    UPDATE(UpdateGenerator(), UpdateParser()),
    INSERT(InsertGenerator(), InsertParser()),
    DELETE(DeleteGenerator(), DeleteParser());

    companion object {
        @JvmStatic
        fun findByRequest(sql: String): QueryType {
            return values().firstOrNull {
                sql.startsWith(it.name, true)
            } ?: throw RuntimeException("Query type not supported for : $sql")
        }
    }

}