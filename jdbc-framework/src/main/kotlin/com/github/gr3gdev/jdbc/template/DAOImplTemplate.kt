package com.github.gr3gdev.jdbc.template

import javax.lang.model.element.Element

object DAOImplTemplate {

    fun generate(packageName: String, imports: String, interfaceName: Element, className: String, methods: String): String {
        return """
package $packageName;

import com.github.gr3gdev.jdbc.SQLDataSource;
import com.github.gr3gdev.jdbc.error.JDBCExecutionException;
import $interfaceName;

$imports

/**
 * Auto-generated class $interfaceName.
 */
class $className implements ${interfaceName.simpleName} {
    $methods
}
""".trimIndent()
    }

}