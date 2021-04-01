package com.github.gr3gdev.jdbc.test.dao;

import com.github.gr3gdev.jdbc.dao.Queries;
import com.github.gr3gdev.jdbc.dao.Query;
import com.github.gr3gdev.jdbc.test.bean.House;
import com.github.gr3gdev.jdbc.test.dao.impl.AbstractHouseDAO;

import java.util.List;
import java.util.Optional;

@Queries(implementation = AbstractHouseDAO.class)
public interface HouseDAO {

    List<House> test();

    @Query(sql = "INSERT House (House)")
    void add(House house);

    @Query(sql = "SELECT House.id, House.name FROM House WHERE House.id")
    Optional<House> findById(int id);

}
