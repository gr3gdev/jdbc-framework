package com.github.gr3gdev.jdbc.test.dao.impl;

import com.github.gr3gdev.jdbc.SQLDataSource;
import com.github.gr3gdev.jdbc.test.bean.House;
import com.github.gr3gdev.jdbc.test.dao.HouseDAO;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractHouseDAO implements HouseDAO {

    @Override
    public List<House> test() {
        return SQLDataSource.executeAndReturn("test2", "SELECT * FROM TABLE_HOUSE", (stm) -> {

        }, (res) -> {
            final List<House> houses = new ArrayList<>();
            while (res.next()) {
                final House house = new House();
                house.setId(res.getInt("ID"));
                house.setName(res.getString("HOUSE_NAME"));
                houses.add(house);
            }
            return houses;
        });
    }

}
