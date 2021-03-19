package com.github.gr3gdev.jdbc.test

import com.github.gr3gdev.jdbc.test.bean.*
import com.github.gr3gdev.jdbc.test.jdbc.JDBCFactory
import java.time.LocalDateTime

fun test1() {
    val carDAO = JDBCFactory.getCarDAO()
    val townDAO = JDBCFactory.getTownDAO()
    val personAddressDAO = JDBCFactory.getPersonAddressDAO()
    val personDAO = JDBCFactory.getPersonDAO()

    println(personDAO.select())

    val town = Town()
    town.name = "Atlanta"
    townDAO.add(town)

    val address = PersonAddress()
    address.street = "5 avenue Charles de Gaulle"
    address.town = town
    personAddressDAO.add(address)

    val car = Car()
    car.label = "Tesla"
    carDAO.add(car)

    val car2 = Car()
    car2.label = "Chrysler"
    carDAO.add(car2)

    val person = Person()
    person.firstname = "Bobby"
    person.personAddress = address
    person.car = car2

    println("Add $person")
    personDAO.add(person)

    println(personDAO.select())
    personDAO.selectByName("Bobby").ifPresent { println(it.id) }
    println(personDAO.selectAddress())

    println("Delete $person")
    personDAO.delete(person)

    println(personDAO.select())
}

fun test2() {
    val houseDAO = JDBCFactory.getHouseDAO()
    val house = House()
    house.name = "MyHouse"
    houseDAO.add(house)

    println(houseDAO.findById(1))
    println(houseDAO.test())
}

fun main() {
    println(LocalDateTime.now())
    DAORepository.init()

    println(LocalDateTime.now())
    test1()
    println(LocalDateTime.now())

    println(LocalDateTime.now())
    test2()
    println(LocalDateTime.now())
}
