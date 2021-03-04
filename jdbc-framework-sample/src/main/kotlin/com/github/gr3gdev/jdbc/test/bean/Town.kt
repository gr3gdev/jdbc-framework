package com.github.gr3gdev.jdbc.test.bean

import com.github.gr3gdev.jdbc.metadata.Column
import com.github.gr3gdev.jdbc.metadata.Table

@Table(databaseName = "test1")
class Town {
    @Column(primaryKey = true, autoincrement = true, sqlType = "LONG")
    var id: Long = 0

    @Column(required = true, sqlType = "VARCHAR(40)")
    lateinit var name: String

    override fun toString(): String {
        return "Town(id=$id, name='$name')"
    }
}