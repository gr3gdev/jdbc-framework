package com.github.gr3gdev.jdbc.generator.element

internal class GetterStructure(val parent: GetterStructure?, val table: TableElement, val column: ColumnElement?) {

    val children = LinkedHashSet<GetterStructure>()
    var joinType = "INNER"
    lateinit var alias: String

}