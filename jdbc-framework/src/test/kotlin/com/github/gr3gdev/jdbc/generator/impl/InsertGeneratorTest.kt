package com.github.gr3gdev.jdbc.generator.impl

import com.github.gr3gdev.jdbc.dao.QueryType
import com.github.gr3gdev.jdbc.generator.AbstractGeneratorTest
import com.github.gr3gdev.jdbc.generator.QueryGenerator
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class InsertGeneratorTest : AbstractGeneratorTest() {

    @Test
    fun testExecuteBatch() {
        initQuery(QueryType.INSERT, "addAll", null,
                listOf(Parameter("java.util.List<com.github.gr3gdev.jdbc.Person>", "persons")),
                null, null)
        val res = QueryGenerator.generate(processingEnvironment, table, "com.github.gr3gdev.jdbc.Person", query)
        Assert.assertTrue("Import missing", res.first.containsAll(listOf("java.sql.Connection", "java.sql.PreparedStatement", "java.sql.ResultSet", "java.sql.SQLException")))
        Assert.assertEquals("""
@Override
public void addAll(final java.util.List<com.github.gr3gdev.jdbc.Person> persons) {
    final String sql = "INSERT INTO PERSON (FIRSTNAME, LASTNAME, BIRTHDATE, ID_ADDRESS) VALUES (?, ?, ?, ?)";
    try (final Connection cnx = SQLDataSource.getConnection("TEST_DB");
        final PreparedStatement stm = cnx.prepareStatement(sql)) {
        int index = 0;
        for (final com.github.gr3gdev.jdbc.Person element : persons) {
            stm.setString(1, element.getFirstname());
            stm.setString(2, element.getLastname());
            stm.setDate(3, element.getBirthdate());
            stm.setLong(4, element.getAddress().getId());
            stm.addBatch();
            index++;
            if (index % 1000 == 0 || index == persons.size()) {
                stm.executeBatch();
            }
        }
    } catch (SQLException throwables) {
        throw new JDBCExecutionException(com.github.gr3gdev.jdbc.dao.QueryType.INSERT, "persons", throwables);
    }
}
        """.trimIndent(), res.second.trimIndent())
    }

    @Test
    fun testExecute() {
        initQuery(QueryType.INSERT, "add", null,
                listOf(Parameter("com.github.gr3gdev.jdbc.Person", "person")),
                null, null)
        val res = QueryGenerator.generate(processingEnvironment, table, null, query)
        Assert.assertTrue("Import missing", res.first.containsAll(listOf("java.sql.Connection", "java.sql.PreparedStatement", "java.sql.ResultSet", "java.sql.SQLException")))
        Assert.assertEquals("""
@Override
public void add(final com.github.gr3gdev.jdbc.Person person) {
    final String sql = "INSERT INTO PERSON (FIRSTNAME, LASTNAME, BIRTHDATE, ID_ADDRESS) VALUES (?, ?, ?, ?)";
    try (final Connection cnx = SQLDataSource.getConnection("TEST_DB");
        final PreparedStatement stm = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        stm.setString(1, person.getFirstname());
        stm.setString(2, person.getLastname());
        stm.setDate(3, person.getBirthdate());
        stm.setLong(4, person.getAddress().getId());
        stm.executeUpdate();
        try (final ResultSet res = stm.getGeneratedKeys()) {
            if (res.next()) {
                person.setId(res.getLong("ID"));
            }
        }
    } catch (SQLException throwables) {
        throw new JDBCExecutionException(com.github.gr3gdev.jdbc.dao.QueryType.INSERT, "person", throwables);
    }
}
        """.trimIndent(), res.second.trimIndent())
    }

}