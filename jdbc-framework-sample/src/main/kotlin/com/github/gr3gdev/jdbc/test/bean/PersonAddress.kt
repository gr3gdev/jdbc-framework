package com.github.gr3gdev.jdbc.test.bean

import com.github.gr3gdev.jdbc.metadata.Column
import com.github.gr3gdev.jdbc.metadata.Table

@Table(databaseName = "test1")
class PersonAddress {
    @Column(primaryKey = true, autoincrement = true, sqlType = "LONG")
    var id: Long = 0

    @Column(required = true, sqlType = "VARCHAR(255)")
    var street: String? = null

    @Column(required = true, sqlType = "LONG")
    var town: Town? = null

    override fun toString(): String {
        return "PersonAddress(id=$id, street='$street', town=$town)"
    }
}