package com.github.gr3gdev.jdbc.test.bean

import com.github.gr3gdev.jdbc.metadata.Column
import com.github.gr3gdev.jdbc.metadata.Table

@Table(databaseName = "test1")
class Car {
    @Column(primaryKey = true, autoincrement = true, sqlType = "LONG")
    var id: Long = 0

    @Column(required = true, sqlType = "VARCHAR(100)")
    var label: String? = null

    @Column(required = true, sqlType = "BOOLEAN")
    var clean: Boolean = true

    override fun toString(): String {
        return "Car(id=$id, label='$label', clean=$clean)"
    }
}