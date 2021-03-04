package com.github.gr3gdev.jdbc.template

object CreateTableTemplate {

    fun generate(packageName: String, imports: String, className: String, methods: String): String {
        return """
package $packageName;

import com.github.gr3gdev.jdbc.SQLDataSource;
import com.github.gr3gdev.jdbc.error.JDBCCreateTableException;

$imports

/**
 * Auto-generated class $className.
 */
class $className {
    $methods
}
""".trimIndent()
    }

}