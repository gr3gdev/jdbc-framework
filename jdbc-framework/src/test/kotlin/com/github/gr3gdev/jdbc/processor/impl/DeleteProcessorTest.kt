package com.github.gr3gdev.jdbc.processor.impl

import com.github.gr3gdev.jdbc.error.JDBCConfigurationException
import com.github.gr3gdev.jdbc.generator.impl.DeleteGenerator
import com.github.gr3gdev.jdbc.processor.AbstractProcessorTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DeleteProcessorTest : AbstractProcessorTest() {

    @Test(expected = JDBCConfigurationException::class)
    fun testExecuteInvalidReturnType() {
        Mockito.`when`(query.returnType).thenReturn(queryReturnType)
        Mockito.`when`(queryReturnType.toString()).thenReturn("java.lang.String")

        val filters = columns.map { it.first }

        DeleteGenerator(tableElement).execute(query, null, filters)
    }

    @Test(expected = JDBCConfigurationException::class)
    fun testExecuteInvalidArguments() {
        DeleteGenerator(tableElement).execute(query, null, null)
    }

    @Test
    fun testExecute() {
        Mockito.`when`(query.returnType).thenReturn(queryReturnType)
        Mockito.`when`(queryReturnType.toString()).thenReturn("int")

        val filters = columns.map { it.first }

        Mockito.`when`(queryName.toString()).thenReturn("deleteAll")

        val res = DeleteGenerator(tableElement).execute(query, null, filters)
        Assert.assertTrue("Import missing", res.first.containsAll(listOf("java.sql.Connection", "java.sql.PreparedStatement", "java.sql.ResultSet", "java.sql.SQLException")))
        Assert.assertEquals("""
    @Override
    public int deleteAll(final com.github.gr3gdev.jdbc.Test test) {
        final String sql = "DELETE FROM TEST WHERE ID = ? AND NAME = ? AND DATE_CREATION = ? AND DATE_BIRTHDAY = ? AND AVERAGE = ? AND ORDER = ? AND ACTIVE = ? AND DELETED = ? AND DOUBLE = ? AND TIME_MODIFIED = ? AND URL = ? AND NUMBER = ? AND SHORT = ? AND TIME = ?";
        try (final Connection cnx = SQLDataSource.getConnection("TestDB");
            final PreparedStatement stm = cnx.prepareStatement(sql)) {
            stm.setLong(1, test.getId());
            stm.setString(2, test.getName());
            stm.setDate(3, test.getDateCreation());
            stm.setDate(4, test.getDateBirthday());
            stm.setFloat(5, test.getAverage());
            stm.setInt(6, test.getOrder());
            stm.setBoolean(7, test.getActive());
            stm.setByte(8, test.getDeleted());
            stm.setDouble(9, test.getDouble());
            stm.setTimestamp(10, test.getTimeModified());
            stm.setURL(11, test.getUrl());
            stm.setBigDecimal(12, test.getNumber());
            stm.setShort(13, test.getShort());
            stm.setTime(14, test.getTime());
            return stm.executeUpdate();
        } catch (SQLException throwables) {
            throw new JDBCExecutionException(com.github.gr3gdev.jdbc.dao.QueryType.DELETE, "test", throwables);
        }
    }
        """.trimIndent(), res.second.trimIndent())

    }

}