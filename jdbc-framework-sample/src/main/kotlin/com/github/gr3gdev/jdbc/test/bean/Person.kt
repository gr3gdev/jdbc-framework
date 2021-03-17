package com.github.gr3gdev.jdbc.test.bean

import com.github.gr3gdev.jdbc.metadata.Column
import com.github.gr3gdev.jdbc.metadata.Table
import java.util.*

@Table(databaseName = "test1")
class Person {
    @Column(primaryKey = true, autoincrement = true)
    var id: Long = 0

    @Column
    lateinit var firstname: String

    @Column
    var age: Int? = null

    @Column
    var birthday: Date? = null

    @Column
    lateinit var personAddress: PersonAddress

    @Column
    var car: Car? = null

    var test = true

    override fun toString(): String {
        return "Person(id=$id, name='$firstname', personAddress=$personAddress, car=$car, test=$test)"
    }

}