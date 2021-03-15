package com.github.gr3gdev.jdbc.template

object DAOImplTemplate {

    fun generate(packageName: String, imports: String, extends: String, className: String, methods: String): String {
        return """
package $packageName;

import com.github.gr3gdev.jdbc.SQLDataSource;

$imports

/**
 * Auto-generated class.
 */
class $className $extends {
    $methods
}
""".trimIndent()
    }

}