package com.github.gr3gdev.jdbc.processor.impl

import com.github.gr3gdev.jdbc.generator.impl.SelectGenerator
import com.github.gr3gdev.jdbc.processor.AbstractProcessorTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SelectProcessorTest : AbstractProcessorTest() {

    @Before
    fun initSelect() {
        Mockito.`when`(query.returnType).thenReturn(queryReturnType)
        Mockito.`when`(queryReturnType.toString()).thenReturn("com.github.gr3gdev.jdbc.Test")
    }

    @Test
    fun testExecuteWithAttributesAndFilters() {
        val attributes = columns.map { it.first }
        val filters = columns.map { it.first }

        Mockito.`when`(queryName.toString()).thenReturn("selectByName")

        val res = SelectGenerator(tableElement).execute(query, attributes, filters)
        assertTrue("Import missing", res.first.containsAll(listOf("java.sql.Connection", "java.sql.PreparedStatement", "java.sql.ResultSet", "java.sql.SQLException")))
        assertEquals("""
    @Override
    public com.github.gr3gdev.jdbc.Test selectByName(final com.github.gr3gdev.jdbc.Test test) {
        final com.github.gr3gdev.jdbc.Test ret = new com.github.gr3gdev.jdbc.Test();
        final String sql = "SELECT ID, NAME, DATE_CREATION, DATE_BIRTHDAY, AVERAGE, ORDER, ACTIVE, DELETED, DOUBLE, TIME_MODIFIED, URL, NUMBER, SHORT, TIME FROM TEST WHERE ID = ? AND NAME = ? AND DATE_CREATION = ? AND DATE_BIRTHDAY = ? AND AVERAGE = ? AND ORDER = ? AND ACTIVE = ? AND DELETED = ? AND DOUBLE = ? AND TIME_MODIFIED = ? AND URL = ? AND NUMBER = ? AND SHORT = ? AND TIME = ?";
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
            try (final ResultSet res = stm.executeQuery()) {
                while (res.next()) {
                    ret.setId(res.getLong("ID"));
                    ret.setName(res.getString("NAME"));
                    ret.setDateCreation(res.getDate("DATE_CREATION"));
                    ret.setDateBirthday(res.getDate("DATE_BIRTHDAY"));
                    ret.setAverage(res.getFloat("AVERAGE"));
                    ret.setOrder(res.getInt("ORDER"));
                    ret.setActive(res.getBoolean("ACTIVE"));
                    ret.setDeleted(res.getByte("DELETED"));
                    ret.setDouble(res.getDouble("DOUBLE"));
                    ret.setTimeModified(res.getTimestamp("TIME_MODIFIED"));
                    ret.setUrl(res.getURL("URL"));
                    ret.setNumber(res.getBigDecimal("NUMBER"));
                    ret.setShort(res.getShort("SHORT"));
                    ret.setTime(res.getTime("TIME"));
                }
                return ret;
            }
        } catch (SQLException throwables) {
            throw new JDBCExecutionException(com.github.gr3gdev.jdbc.dao.QueryType.SELECT, "test", throwables);
        }
    }
        """.trimIndent(), res.second.trimIndent())
    }

    @Test
    fun testExecuteWithAttributes() {
        val attributes = listOf(ID.first, NAME.first, DATE_CREATION.first)

        Mockito.`when`(queryName.toString()).thenReturn("selectAll")

        val res = SelectGenerator(tableElement).execute(query, attributes, null)
        assertTrue("Import missing", res.first.containsAll(listOf("java.sql.Connection", "java.sql.PreparedStatement", "java.sql.ResultSet", "java.sql.SQLException")))
        assertEquals("""
    @Override
    public com.github.gr3gdev.jdbc.Test selectAll(final com.github.gr3gdev.jdbc.Test test) {
        final com.github.gr3gdev.jdbc.Test ret = new com.github.gr3gdev.jdbc.Test();
        final String sql = "SELECT ID, NAME, DATE_CREATION FROM TEST";
        try (final Connection cnx = SQLDataSource.getConnection("TestDB");
            final PreparedStatement stm = cnx.prepareStatement(sql)) {
            // Without conditions
            try (final ResultSet res = stm.executeQuery()) {
                while (res.next()) {
                    ret.setId(res.getLong("ID"));
                    ret.setName(res.getString("NAME"));
                    ret.setDateCreation(res.getDate("DATE_CREATION"));
                }
                return ret;
            }
        } catch (SQLException throwables) {
            throw new JDBCExecutionException(com.github.gr3gdev.jdbc.dao.QueryType.SELECT, "test", throwables);
        }
    }
        """.trimIndent(), res.second.trimIndent())
    }

    @Test
    fun testExecute() {
        Mockito.`when`(queryName.toString()).thenReturn("select")

        val res = SelectGenerator(tableElement).execute(query, null, null)
        assertTrue("Import missing", res.first.containsAll(listOf("java.sql.Connection", "java.sql.PreparedStatement", "java.sql.ResultSet", "java.sql.SQLException")))
        assertEquals("""
    @Override
    public com.github.gr3gdev.jdbc.Test select(final com.github.gr3gdev.jdbc.Test test) {
        final com.github.gr3gdev.jdbc.Test ret = new com.github.gr3gdev.jdbc.Test();
        final String sql = "SELECT * FROM TEST";
        try (final Connection cnx = SQLDataSource.getConnection("TestDB");
            final PreparedStatement stm = cnx.prepareStatement(sql)) {
            // Without conditions
            try (final ResultSet res = stm.executeQuery()) {
                while (res.next()) {
                    ret.setId(res.getLong("ID"));
                    ret.setName(res.getString("NAME"));
                    ret.setDateCreation(res.getDate("DATE_CREATION"));
                    ret.setDateBirthday(res.getDate("DATE_BIRTHDAY"));
                    ret.setAverage(res.getFloat("AVERAGE"));
                    ret.setOrder(res.getInt("ORDER"));
                    ret.setActive(res.getBoolean("ACTIVE"));
                    ret.setDeleted(res.getByte("DELETED"));
                    ret.setDouble(res.getDouble("DOUBLE"));
                    ret.setTimeModified(res.getTimestamp("TIME_MODIFIED"));
                    ret.setUrl(res.getURL("URL"));
                    ret.setNumber(res.getBigDecimal("NUMBER"));
                    ret.setShort(res.getShort("SHORT"));
                    ret.setTime(res.getTime("TIME"));
                }
                return ret;
            }
        } catch (SQLException throwables) {
            throw new JDBCExecutionException(com.github.gr3gdev.jdbc.dao.QueryType.SELECT, "test", throwables);
        }
    }
        """.trimIndent(), res.second.trimIndent())
    }
}