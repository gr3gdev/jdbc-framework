package com.github.gr3gdev.jdbc.template

import javax.lang.model.element.Element

object JDBCFactoryTemplate {

    fun generate(packageName: String, imports: String, className: String, initMethodContent: String?, daoImplementations: String): String {
        return """
package $packageName;

import com.github.gr3gdev.jdbc.SQLDataSource;

$imports

/**
 * Auto-generated class.
 */
public class $className {

    /**
     * Init the database(s).
     */
    public static void init() {
        $initMethodContent
    }

    // DAO implementations
$daoImplementations
    // end DAO implementations

}
""".trimIndent()
    }

    fun getter(it: Pair<Element, String?>): String {
        return """
    /**
     * Get an instance of {@link ${it.first}}.
     *
     * @return {@link ${it.first}}
     */
    public static ${it.first.simpleName} get${it.first.simpleName}() {
        return new ${it.second}();
    }"""
    }

}