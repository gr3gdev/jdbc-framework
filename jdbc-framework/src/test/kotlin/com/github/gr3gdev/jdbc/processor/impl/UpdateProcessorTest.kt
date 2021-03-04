package com.github.gr3gdev.jdbc.processor.impl

import com.github.gr3gdev.jdbc.error.JDBCConfigurationException
import com.github.gr3gdev.jdbc.generator.impl.UpdateGenerator
import com.github.gr3gdev.jdbc.processor.AbstractProcessorTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class UpdateProcessorTest: AbstractProcessorTest() {

    @Test(expected = JDBCConfigurationException::class)
    fun testExecuteInvalidReturnType() {
        Mockito.`when`(query.returnType).thenReturn(queryReturnType)
        Mockito.`when`(queryReturnType.toString()).thenReturn("java.lang.String")

        val attributes = columns.map { it.first }

        UpdateGenerator(tableElement).execute(query, attributes, null)
    }

    @Test(expected = JDBCConfigurationException::class)
    fun testExecuteInvalidArguments() {
        UpdateGenerator(tableElement).execute(query, null, null)
    }

    @Test
    fun testExecute() {
        Mockito.`when`(query.returnType).thenReturn(queryReturnType)
        Mockito.`when`(queryReturnType.toString()).thenReturn("int")

        val attributes = columns.map { it.first }

        Mockito.`when`(queryName.toString()).thenReturn("updateAll")

        val res = UpdateGenerator(tableElement).execute(query, attributes, null)
        Assert.assertTrue("Import missing", res.first.containsAll(listOf("java.sql.Connection", "java.sql.PreparedStatement", "java.sql.ResultSet", "java.sql.SQLException")))
        Assert.assertEquals("""
    @Override
    public int updateAll(final com.github.gr3gdev.jdbc.Test test) {
        final String sql = "UPDATE TEST SET ID = ?, NAME = ?, DATE_CREATION = ?, DATE_BIRTHDAY = ?, AVERAGE = ?, ORDER = ?, ACTIVE = ?, DELETED = ?, DOUBLE = ?, TIME_MODIFIED = ?, URL = ?, NUMBER = ?, SHORT = ?, TIME = ?";
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
            throw new JDBCExecutionException(com.github.gr3gdev.jdbc.dao.QueryType.UPDATE, "test", throwables);
        }
    }
        """.trimIndent(), res.second.trimIndent())

    }

    @Test
    fun testExecuteWithAttributesAndFilters() {
        Mockito.`when`(query.returnType).thenReturn(queryReturnType)
        Mockito.`when`(queryReturnType.toString()).thenReturn("int")

        val attributes = columns.map { it.first }
        val filters = columns.map { it.first }

        Mockito.`when`(queryName.toString()).thenReturn("updateSpecific")

        val res = UpdateGenerator(tableElement).execute(query, attributes, filters)
        Assert.assertTrue("Import missing", res.first.containsAll(listOf("java.sql.Connection", "java.sql.PreparedStatement", "java.sql.ResultSet", "java.sql.SQLException")))
        Assert.assertEquals("""
    @Override
    public int updateSpecific(final com.github.gr3gdev.jdbc.Test test) {
        final String sql = "UPDATE TEST SET ID = ?, NAME = ?, DATE_CREATION = ?, DATE_BIRTHDAY = ?, AVERAGE = ?, ORDER = ?, ACTIVE = ?, DELETED = ?, DOUBLE = ?, TIME_MODIFIED = ?, URL = ?, NUMBER = ?, SHORT = ?, TIME = ? WHERE ID = ? AND NAME = ? AND DATE_CREATION = ? AND DATE_BIRTHDAY = ? AND AVERAGE = ? AND ORDER = ? AND ACTIVE = ? AND DELETED = ? AND DOUBLE = ? AND TIME_MODIFIED = ? AND URL = ? AND NUMBER = ? AND SHORT = ? AND TIME = ?";
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
            stm.setLong(15, test.getId());
            stm.setString(16, test.getName());
            stm.setDate(17, test.getDateCreation());
            stm.setDate(18, test.getDateBirthday());
            stm.setFloat(19, test.getAverage());
            stm.setInt(20, test.getOrder());
            stm.setBoolean(21, test.getActive());
            stm.setByte(22, test.getDeleted());
            stm.setDouble(23, test.getDouble());
            stm.setTimestamp(24, test.getTimeModified());
            stm.setURL(25, test.getUrl());
            stm.setBigDecimal(26, test.getNumber());
            stm.setShort(27, test.getShort());
            stm.setTime(28, test.getTime());
            return stm.executeUpdate();
        } catch (SQLException throwables) {
            throw new JDBCExecutionException(com.github.gr3gdev.jdbc.dao.QueryType.UPDATE, "test", throwables);
        }
    }
        """.trimIndent(), res.second.trimIndent())

    }

}