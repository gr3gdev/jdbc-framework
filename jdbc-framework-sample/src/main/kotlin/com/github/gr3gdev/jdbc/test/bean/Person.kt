package com.github.gr3gdev.jdbc.test.bean

import com.github.gr3gdev.jdbc.metadata.Column
import com.github.gr3gdev.jdbc.metadata.Table
import java.util.*

@Table(databaseName = "test1")
class Person {
    @Column(primaryKey = true, autoincrement = true, sqlType = "LONG")
    var id: Long = 0

    @Column(sqlType = "VARCHAR(40)")
    lateinit var firstname: String

    @Column(required = false, sqlType = "INT")
    var age: Int? = null

    @Column(required = false, sqlType = "BOOLEAN")
    var birthday: Date? = null

    @Column(required = true, sqlType = "LONG")
    lateinit var personAddress: PersonAddress

    @Column(required = false, sqlType = "LONG")
    var car: Car? = null

    var test = true

    override fun toString(): String {
        return "Person(id=$id, name='$firstname', personAddress=$personAddress, car=$car, test=$test)"
    }

}