package com.github.gr3gdev.jdbc.test.bean;

import com.github.gr3gdev.jdbc.metadata.Column;
import com.github.gr3gdev.jdbc.metadata.Table;

@Table(name = "TABLE_HOUSE", databaseName = "test2")
public class House {

    @Column(primaryKey = true, autoincrement = true)
    private int id;

    @Column(name = "HOUSE_NAME")
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "House{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
