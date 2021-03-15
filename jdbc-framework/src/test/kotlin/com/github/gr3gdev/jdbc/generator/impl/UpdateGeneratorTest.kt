package com.github.gr3gdev.jdbc.generator.impl

import com.github.gr3gdev.jdbc.dao.QueryType
import com.github.gr3gdev.jdbc.error.JDBCConfigurationException
import com.github.gr3gdev.jdbc.generator.AbstractGeneratorTest
import com.github.gr3gdev.jdbc.generator.QueryGenerator
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class UpdateGeneratorTest : AbstractGeneratorTest() {

    @Test(expected = JDBCConfigurationException::class)
    fun testExecuteErrorAttributes() {
        initQuery(QueryType.UPDATE, "update", "int", null, null, null)
        QueryGenerator.generate(processingEnvironment, table, "com.github.gr3gdev.jdbc.test.Person", query)
    }

    @Test(expected = JDBCConfigurationException::class)
    fun testExecuteErrorReturnType() {
        initQuery(QueryType.UPDATE, "update", "Person", null, null, null)
        QueryGenerator.generate(processingEnvironment, table, "com.github.gr3gdev.jdbc.test.Person", query)
    }

    @Test
    fun testExecute() {
        initQuery(QueryType.UPDATE, "update", "int",
                listOf(Parameter("com.github.gr3gdev.jdbc.test.Person", "person")),
                listOf("lastname", "firstname", "address"), listOf("id"))
        val res = QueryGenerator.generate(processingEnvironment, table, "com.github.gr3gdev.jdbc.test.Person", query)
        Assert.assertTrue("Import missing", res.first.containsAll(listOf("java.sql.Connection", "java.sql.PreparedStatement", "java.sql.ResultSet", "java.sql.SQLException")))
        Assert.assertEquals("""
@Override
public int update(final com.github.gr3gdev.jdbc.test.Person person) {
    final String sql = "UPDATE PERSON person_1 SET person_1.ID_ADDRESS = ?, person_1.FIRSTNAME = ?, person_1.LASTNAME = ? WHERE person_1.ID = ?";
    return SQLDataSource.executeAndUpdate("TEST_DB", sql, (stm) -> {
        stm.setLong(1, person.getAddress().getId());
        stm.setString(2, person.getFirstname());
        stm.setString(3, person.getLastname());
        stm.setLong(4, person.getId());
        return stm.executeUpdate();
    });
}
        """.trimIndent(), res.second.trimIndent())
    }

    @Test
    fun testExecuteParameters() {
        initQuery(QueryType.UPDATE, "update", "int",
                listOf(Parameter("java.lang.String", "lastname"), Parameter("java.lang.String", "firstname"), Parameter("java.lang.Long", "id")),
                listOf("lastname", "firstname"), listOf("id"))
        val res = QueryGenerator.generate(processingEnvironment, table, "com.github.gr3gdev.jdbc.test.Person", query)
        Assert.assertTrue("Import missing", res.first.containsAll(listOf("java.sql.Connection", "java.sql.PreparedStatement", "java.sql.ResultSet", "java.sql.SQLException")))
        Assert.assertEquals("""
@Override
public int update(final java.lang.String lastname, final java.lang.String firstname, final java.lang.Long id) {
    final String sql = "UPDATE PERSON person_1 SET person_1.FIRSTNAME = ?, person_1.LASTNAME = ? WHERE person_1.ID = ?";
    return SQLDataSource.executeAndUpdate("TEST_DB", sql, (stm) -> {
        stm.setString(1, firstname);
        stm.setString(2, lastname);
        stm.setLong(3, id);
        return stm.executeUpdate();
    });
}
        """.trimIndent(), res.second.trimIndent())
    }

}