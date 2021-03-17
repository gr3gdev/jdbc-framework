package com.github.gr3gdev.jdbc.generator.impl

import com.github.gr3gdev.jdbc.dao.QueryJoinType
import com.github.gr3gdev.jdbc.dao.QueryType
import com.github.gr3gdev.jdbc.error.JDBCConfigurationException
import com.github.gr3gdev.jdbc.generator.AbstractGeneratorTest
import com.github.gr3gdev.jdbc.generator.QueryGenerator
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import javax.lang.model.element.AnnotationMirror

@RunWith(MockitoJUnitRunner::class)
class SelectGeneratorTest : AbstractGeneratorTest() {

    @Test
    fun testExecuteInvalidReturn() {
        initQuery(QueryType.SELECT, "selectById", "com.github.gr3gdev.jdbc.test.Person",
                null, null, listOf("id"))
        try {
            QueryGenerator.generate(processingEnvironment, table, "com.github.gr3gdev.jdbc.test.Person", query)
            Assert.fail()
        } catch (exc: JDBCConfigurationException) {
            Assert.assertEquals("Select an unique object must return Optional<com.github.gr3gdev.jdbc.test.Person>", exc.message)
        }
    }

    @Test
    fun testExecute() {
        initQuery(QueryType.SELECT, "selectAll", "java.util.List<com.github.gr3gdev.jdbc.test.Person>",
                null, null, null)
        val res = QueryGenerator.generate(processingEnvironment, table, "com.github.gr3gdev.jdbc.test.Person", query)
        Assert.assertTrue("Import missing", res.first.containsAll(listOf("java.sql.Connection", "java.sql.PreparedStatement", "java.sql.ResultSet", "java.sql.SQLException")))
        Assert.assertEquals("""
@Override
public java.util.List<com.github.gr3gdev.jdbc.test.Person> selectAll() {
    final java.util.List<com.github.gr3gdev.jdbc.test.Person> ret = new java.util.ArrayList();
    final String sql = "SELECT person_1.ID_ADDRESS, person_1.BIRTHDATE, person_1.FIRSTNAME, person_1.ID, person_1.LASTNAME FROM PERSON person_1";
    return SQLDataSource.executeAndReturn("TEST_DB", sql, (stm) -> {
        // Without conditions
    }, (res) -> {
        while (res.next()) {
            final com.github.gr3gdev.jdbc.test.Person elt = new com.github.gr3gdev.jdbc.test.Person();
            final com.github.gr3gdev.jdbc.test.Address address = new com.github.gr3gdev.jdbc.test.Address();
            address.setId(res.getLong("ID_ADDRESS"));
            elt.setAddress(address);
            elt.setBirthdate(res.getDate("BIRTHDATE"));
            elt.setFirstname(res.getString("FIRSTNAME"));
            elt.setId(res.getLong("ID"));
            elt.setLastname(res.getString("LASTNAME"));
            ret.add(elt);
        }
        return ret;
    });
}
        """.trimIndent(), res.second.trimIndent())
    }

    @Test
    fun testExecuteWithJoin() {
        initQuery(QueryType.SELECT, "selectPersonAndAddress", "java.util.List<com.github.gr3gdev.jdbc.test.Person>",
                null, listOf("firstname", "lastname", "address", "address.street", "address.town"), null)
        val res = QueryGenerator.generate(processingEnvironment, table, "com.github.gr3gdev.jdbc.test.Person", query)
        Assert.assertTrue("Import missing", res.first.containsAll(listOf("java.sql.Connection", "java.sql.PreparedStatement", "java.sql.ResultSet", "java.sql.SQLException")))
        Assert.assertEquals("""
@Override
public java.util.List<com.github.gr3gdev.jdbc.test.Person> selectPersonAndAddress() {
    final java.util.List<com.github.gr3gdev.jdbc.test.Person> ret = new java.util.ArrayList();
    final String sql = "SELECT address_1.STREET, address_1.ID_TOWN, person_1.ID_ADDRESS, person_1.FIRSTNAME, person_1.LASTNAME FROM PERSON person_1 INNER JOIN ADDRESS address_1 ON address_1.ID = person_1.ID_ADDRESS";
    return SQLDataSource.executeAndReturn("TEST_DB", sql, (stm) -> {
        // Without conditions
    }, (res) -> {
        while (res.next()) {
            final com.github.gr3gdev.jdbc.test.Person elt = new com.github.gr3gdev.jdbc.test.Person();
            final com.github.gr3gdev.jdbc.test.Address address = new com.github.gr3gdev.jdbc.test.Address();
            address.setId(res.getLong("ID_ADDRESS"));
            elt.setAddress(address);
            address.setStreet(res.getString("STREET"));
            final com.github.gr3gdev.jdbc.test.Town town = new com.github.gr3gdev.jdbc.test.Town();
            town.setId(res.getLong("ID_TOWN"));
            address.setTown(town);
            elt.setFirstname(res.getString("FIRSTNAME"));
            elt.setLastname(res.getString("LASTNAME"));
            ret.add(elt);
        }
        return ret;
    });
}
        """.trimIndent(), res.second.trimIndent())
    }

    @Test
    fun testExecuteWithLeftJoin() {
        val leftJoin = Mockito.mock(AnnotationMirror::class.java)
        mockAnnotationAttributes(leftJoin, mapOf(
                "type" to QueryJoinType.LEFT,
                "table" to "com.github.gr3gdev.jdbc.test.Address"
        ))

        initQuery(QueryType.SELECT, "selectPersonLeftAddress", "java.util.List<com.github.gr3gdev.jdbc.test.Person>",
                null, listOf("firstname", "lastname", "address", "address.street", "address.town"), null,
                listOf(leftJoin))
        val res = QueryGenerator.generate(processingEnvironment, table, "com.github.gr3gdev.jdbc.test.Person", query)
        Assert.assertTrue("Import missing", res.first.containsAll(listOf("java.sql.Connection", "java.sql.PreparedStatement", "java.sql.ResultSet", "java.sql.SQLException")))
        Assert.assertEquals("""
@Override
public java.util.List<com.github.gr3gdev.jdbc.test.Person> selectPersonLeftAddress() {
    final java.util.List<com.github.gr3gdev.jdbc.test.Person> ret = new java.util.ArrayList();
    final String sql = "SELECT address_1.STREET, address_1.ID_TOWN, person_1.ID_ADDRESS, person_1.FIRSTNAME, person_1.LASTNAME FROM PERSON person_1 LEFT JOIN ADDRESS address_1 ON address_1.ID = person_1.ID_ADDRESS";
    return SQLDataSource.executeAndReturn("TEST_DB", sql, (stm) -> {
        // Without conditions
    }, (res) -> {
        while (res.next()) {
            final com.github.gr3gdev.jdbc.test.Person elt = new com.github.gr3gdev.jdbc.test.Person();
            final com.github.gr3gdev.jdbc.test.Address address = new com.github.gr3gdev.jdbc.test.Address();
            address.setId(res.getLong("ID_ADDRESS"));
            elt.setAddress(address);
            address.setStreet(res.getString("STREET"));
            final com.github.gr3gdev.jdbc.test.Town town = new com.github.gr3gdev.jdbc.test.Town();
            town.setId(res.getLong("ID_TOWN"));
            address.setTown(town);
            elt.setFirstname(res.getString("FIRSTNAME"));
            elt.setLastname(res.getString("LASTNAME"));
            ret.add(elt);
        }
        return ret;
    });
}
        """.trimIndent(), res.second.trimIndent())
    }

    @Test
    fun testExecuteWithRightJoin() {
        val rightJoin = Mockito.mock(AnnotationMirror::class.java)
        mockAnnotationAttributes(rightJoin, mapOf(
                "type" to QueryJoinType.RIGHT,
                "table" to "com.github.gr3gdev.jdbc.test.Address"
        ))

        initQuery(QueryType.SELECT, "selectPersonRightAddress", "java.util.List<com.github.gr3gdev.jdbc.test.Person>",
                null, listOf("firstname", "lastname", "address", "address.street", "address.town"), null,
                listOf(rightJoin))
        val res = QueryGenerator.generate(processingEnvironment, table, "com.github.gr3gdev.jdbc.test.Person", query)
        Assert.assertTrue("Import missing", res.first.containsAll(listOf("java.sql.Connection", "java.sql.PreparedStatement", "java.sql.ResultSet", "java.sql.SQLException")))
        Assert.assertEquals("""
@Override
public java.util.List<com.github.gr3gdev.jdbc.test.Person> selectPersonRightAddress() {
    final java.util.List<com.github.gr3gdev.jdbc.test.Person> ret = new java.util.ArrayList();
    final String sql = "SELECT address_1.STREET, address_1.ID_TOWN, person_1.ID_ADDRESS, person_1.FIRSTNAME, person_1.LASTNAME FROM PERSON person_1 RIGHT JOIN ADDRESS address_1 ON address_1.ID = person_1.ID_ADDRESS";
    return SQLDataSource.executeAndReturn("TEST_DB", sql, (stm) -> {
        // Without conditions
    }, (res) -> {
        while (res.next()) {
            final com.github.gr3gdev.jdbc.test.Person elt = new com.github.gr3gdev.jdbc.test.Person();
            final com.github.gr3gdev.jdbc.test.Address address = new com.github.gr3gdev.jdbc.test.Address();
            address.setId(res.getLong("ID_ADDRESS"));
            elt.setAddress(address);
            address.setStreet(res.getString("STREET"));
            final com.github.gr3gdev.jdbc.test.Town town = new com.github.gr3gdev.jdbc.test.Town();
            town.setId(res.getLong("ID_TOWN"));
            address.setTown(town);
            elt.setFirstname(res.getString("FIRSTNAME"));
            elt.setLastname(res.getString("LASTNAME"));
            ret.add(elt);
        }
        return ret;
    });
}
        """.trimIndent(), res.second.trimIndent())
    }

    @Test
    fun testExecuteWithDoubleJoin() {
        initQuery(QueryType.SELECT, "selectPersonAddressTown", "java.util.Optional<com.github.gr3gdev.jdbc.test.Person>",
                listOf(Parameter("java.lang.Long", "id")), listOf("firstname", "lastname", "address.street", "address.town.name"), listOf("id"))
        val res = QueryGenerator.generate(processingEnvironment, table, "com.github.gr3gdev.jdbc.test.Person", query)
        Assert.assertTrue("Import missing", res.first.containsAll(listOf("java.sql.Connection", "java.sql.PreparedStatement", "java.sql.ResultSet", "java.sql.SQLException")))
        Assert.assertEquals("""
@Override
public java.util.Optional<com.github.gr3gdev.jdbc.test.Person> selectPersonAddressTown(final java.lang.Long id) {
    final String sql = "SELECT address_1.STREET, town_1.NAME, address_1.ID_TOWN, person_1.ID_ADDRESS, person_1.FIRSTNAME, person_1.LASTNAME FROM PERSON person_1 INNER JOIN ADDRESS address_1 ON address_1.ID = person_1.ID_ADDRESS INNER JOIN TOWN town_1 ON town_1.ID = address_1.ID_TOWN WHERE person_1.ID = ?";
    return SQLDataSource.executeAndReturn("TEST_DB", sql, (stm) -> {
        stm.setLong(1, id);
    }, (res) -> {
        final java.util.Optional<com.github.gr3gdev.jdbc.test.Person> ret;
        if (res.next()) {
            final com.github.gr3gdev.jdbc.test.Person elt = new com.github.gr3gdev.jdbc.test.Person();
            final com.github.gr3gdev.jdbc.test.Address address = new com.github.gr3gdev.jdbc.test.Address();
            address.setId(res.getLong("ID_ADDRESS"));
            elt.setAddress(address);
            address.setStreet(res.getString("STREET"));
            final com.github.gr3gdev.jdbc.test.Town town = new com.github.gr3gdev.jdbc.test.Town();
            town.setId(res.getLong("ID_TOWN"));
            address.setTown(town);
            town.setName(res.getString("NAME"));
            elt.setFirstname(res.getString("FIRSTNAME"));
            elt.setLastname(res.getString("LASTNAME"));
            ret = java.util.Optional.of(elt);
            return ret;
        }
        return java.util.Optional.empty();
    });
}
        """.trimIndent(), res.second.trimIndent())
    }

}