package com.github.gr3gdev.jdbc

class ColumnTest(val name: String, val clazz: String?, val primaryKey: Boolean = false, val columns: List<ColumnTest>? = null, val fkType: String? = null)
