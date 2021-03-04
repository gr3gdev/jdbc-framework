package com.github.gr3gdev.jdbc.test.bean

import com.github.gr3gdev.jdbc.metadata.Column
import com.github.gr3gdev.jdbc.metadata.Table

@Table(databaseName = "test1")
class Pet {
    @Column(primaryKey = true, sqlType = "LONG")
    var id: Long = 0

    @Column(sqlType = "VARCHAR(20)")
    var name: String = ""

    @Column(required = true, sqlType = "LONG")
    lateinit var person: Person

    override fun toString(): String {
        return "Pet(id=$id, name='$name', person=$person)"
    }
}