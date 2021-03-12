package com.github.gr3gdev.jdbc.test.dao;

import com.github.gr3gdev.jdbc.dao.Queries;
import com.github.gr3gdev.jdbc.dao.Query;
import com.github.gr3gdev.jdbc.dao.QueryType;
import com.github.gr3gdev.jdbc.test.bean.House;

import java.util.Optional;

@Queries(mapTo = House.class)
public interface HouseDAO {

    @Query(type = QueryType.INSERT)
    void add(House house);

    @Query(type = QueryType.SELECT, attributes = {"id", "name"}, filters = {"id"})
    Optional<House> findById(int id);

}
