package com.github.gr3gdev.jdbc.generator.impl

import com.github.gr3gdev.jdbc.dao.QueryType
import com.github.gr3gdev.jdbc.generator.AbstractGeneratorTest
import com.github.gr3gdev.jdbc.generator.QueryGenerator
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SelectGeneratorTest : AbstractGeneratorTest() {

    @Test
    fun testExecute() {
        initQuery(QueryType.SELECT, "selectAll", "java.util.List<com.github.gr3gdev.jdbc.Person>",
                null, null, null)
        val res = QueryGenerator.generate(processingEnvironment, table, "com.github.gr3gdev.jdbc.Person", query)
        Assert.assertTrue("Import missing", res.first.containsAll(listOf("java.sql.Connection", "java.sql.PreparedStatement", "java.sql.ResultSet", "java.sql.SQLException")))
        Assert.assertEquals("""
@Override
public java.util.List<com.github.gr3gdev.jdbc.Person> selectAll() {
    final java.util.List<com.github.gr3gdev.jdbc.Person> ret = new java.util.ArrayList();
    final String sql = "SELECT person_1.ID_ADDRESS, person_1.BIRTHDATE, person_1.FIRSTNAME, person_1.ID, person_1.LASTNAME FROM PERSON person_1";
    try (final Connection cnx = SQLDataSource.getConnection("TEST_DB");
        final PreparedStatement stm = cnx.prepareStatement(sql)) {
        // Without conditions
        try (final ResultSet res = stm.executeQuery()) {
            while (res.next()) {
                final com.github.gr3gdev.jdbc.Person elt = new com.github.gr3gdev.jdbc.Person();
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
        }
    } catch (SQLException throwables) {
        throw new JDBCExecutionException(com.github.gr3gdev.jdbc.dao.QueryType.SELECT, "", throwables);
    }
}
        """.trimIndent(), res.second.trimIndent())
    }

    @Test
    fun testExecuteWithJoin() {
        initQuery(QueryType.SELECT, "selectPersonAndAddress", "java.util.List<com.github.gr3gdev.jdbc.Person>",
                null, listOf("firstname", "lastname", "address", "address.street", "address.town"), null)
        val res = QueryGenerator.generate(processingEnvironment, table, "com.github.gr3gdev.jdbc.Person", query)
        Assert.assertTrue("Import missing", res.first.containsAll(listOf("java.sql.Connection", "java.sql.PreparedStatement", "java.sql.ResultSet", "java.sql.SQLException")))
        Assert.assertEquals("""
@Override
public java.util.List<com.github.gr3gdev.jdbc.Person> selectPersonAndAddress() {
    final java.util.List<com.github.gr3gdev.jdbc.Person> ret = new java.util.ArrayList();
    final String sql = "SELECT address_1.STREET, address_1.ID_TOWN, person_1.ID_ADDRESS, person_1.FIRSTNAME, person_1.LASTNAME FROM PERSON person_1 INNER JOIN ADDRESS address_1 ON address_1.ID = person_1.ID_ADDRESS";
    try (final Connection cnx = SQLDataSource.getConnection("TEST_DB");
        final PreparedStatement stm = cnx.prepareStatement(sql)) {
        // Without conditions
        try (final ResultSet res = stm.executeQuery()) {
            while (res.next()) {
                final com.github.gr3gdev.jdbc.Person elt = new com.github.gr3gdev.jdbc.Person();
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
        }
    } catch (SQLException throwables) {
        throw new JDBCExecutionException(com.github.gr3gdev.jdbc.dao.QueryType.SELECT, "", throwables);
    }
}
        """.trimIndent(), res.second.trimIndent())
    }

    @Test
    fun testExecuteWithDoubleJoin() {
        initQuery(QueryType.SELECT, "selectPersonAddressTown", "java.util.Optional<com.github.gr3gdev.jdbc.Person>",
                listOf(Parameter("java.lang.Long", "id")), listOf("firstname", "lastname", "address.street", "address.town.name"), listOf("id"))
        val res = QueryGenerator.generate(processingEnvironment, table, "com.github.gr3gdev.jdbc.Person", query)
        Assert.assertTrue("Import missing", res.first.containsAll(listOf("java.sql.Connection", "java.sql.PreparedStatement", "java.sql.ResultSet", "java.sql.SQLException")))
        Assert.assertEquals("""
@Override
public java.util.Optional<com.github.gr3gdev.jdbc.Person> selectPersonAddressTown(final java.lang.Long id) {
    java.util.Optional<com.github.gr3gdev.jdbc.Person> ret = java.util.Optional.empty();
    final String sql = "SELECT address_1.STREET, town_1.NAME, address_1.ID_TOWN, person_1.ID_ADDRESS, person_1.FIRSTNAME, person_1.LASTNAME FROM PERSON person_1 INNER JOIN ADDRESS address_1 ON address_1.ID = person_1.ID_ADDRESS INNER JOIN TOWN town_1 ON town_1.ID = address_1.ID_TOWN WHERE person_1.ID = ?";
    try (final Connection cnx = SQLDataSource.getConnection("TEST_DB");
        final PreparedStatement stm = cnx.prepareStatement(sql)) {
        stm.setLong(1, id);
        try (final ResultSet res = stm.executeQuery()) {
            if (res.next()) {
                final com.github.gr3gdev.jdbc.Person elt = new com.github.gr3gdev.jdbc.Person();
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
            }
            return ret;
        }
    } catch (SQLException throwables) {
        throw new JDBCExecutionException(com.github.gr3gdev.jdbc.dao.QueryType.SELECT, "id", throwables);
    }
}
        """.trimIndent(), res.second.trimIndent())
    }

}