package com.github.gr3gdev.jdbc.generator.impl

import com.github.gr3gdev.jdbc.dao.QueryType
import com.github.gr3gdev.jdbc.generator.AbstractGeneratorTest
import com.github.gr3gdev.jdbc.generator.QueryGenerator
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DeleteGeneratorTest : AbstractGeneratorTest() {

    @Test
    fun testExecute() {
        initQuery(QueryType.DELETE, "delete", "int",
                listOf(Parameter("com.github.gr3gdev.jdbc.Person", "person")),
                null, listOf("id"))
        val res = QueryGenerator.generate(processingEnvironment, table, "com.github.gr3gdev.jdbc.Person", query)
        Assert.assertTrue("Import missing", res.first.containsAll(listOf("java.sql.Connection", "java.sql.PreparedStatement", "java.sql.ResultSet", "java.sql.SQLException")))
        Assert.assertEquals("""
@Override
public int delete(final com.github.gr3gdev.jdbc.Person person) {
    final String sql = "DELETE FROM PERSON person_1 WHERE person_1.ID = ?";
    try (final Connection cnx = SQLDataSource.getConnection("TEST_DB");
        final PreparedStatement stm = cnx.prepareStatement(sql)) {
        stm.setLong(1, person.getId());
        return stm.executeUpdate();
    } catch (SQLException throwables) {
        throw new JDBCExecutionException(com.github.gr3gdev.jdbc.dao.QueryType.DELETE, "person", throwables);
    }
}
        """.trimIndent(), res.second.trimIndent())
    }

    @Test
    fun testExecuteParemeterId() {
        initQuery(QueryType.DELETE, "deleteById", "int",
                listOf(Parameter("java.lang.Long", "id")),
                null, listOf("id"))
        val res = QueryGenerator.generate(processingEnvironment, table, "com.github.gr3gdev.jdbc.Person", query)
        Assert.assertTrue("Import missing", res.first.containsAll(listOf("java.sql.Connection", "java.sql.PreparedStatement", "java.sql.ResultSet", "java.sql.SQLException")))
        Assert.assertEquals("""
@Override
public int deleteById(final java.lang.Long id) {
    final String sql = "DELETE FROM PERSON person_1 WHERE person_1.ID = ?";
    try (final Connection cnx = SQLDataSource.getConnection("TEST_DB");
        final PreparedStatement stm = cnx.prepareStatement(sql)) {
        stm.setLong(1, id);
        return stm.executeUpdate();
    } catch (SQLException throwables) {
        throw new JDBCExecutionException(com.github.gr3gdev.jdbc.dao.QueryType.DELETE, "id", throwables);
    }
}
        """.trimIndent(), res.second.trimIndent())
    }

}