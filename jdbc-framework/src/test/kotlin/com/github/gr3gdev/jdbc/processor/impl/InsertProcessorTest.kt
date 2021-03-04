package com.github.gr3gdev.jdbc.processor.impl

import com.github.gr3gdev.jdbc.generator.impl.InsertGenerator
import com.github.gr3gdev.jdbc.processor.AbstractProcessorTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class InsertProcessorTest : AbstractProcessorTest() {

    @Test
    fun testExecuteBatch() {
        Mockito.`when`(testParameterName.toString()).thenReturn("liste")
        Mockito.`when`(testParameterType.toString()).thenReturn("java.util.List<com.github.gr3gdev.jdbc.Test>")
        Mockito.`when`(queryName.toString()).thenReturn("insertBatch")

        val res = InsertGenerator(tableElement).execute(query, null, null)
        Assert.assertTrue("Import missing", res.first.containsAll(listOf("java.sql.Connection", "java.sql.PreparedStatement", "java.sql.ResultSet", "java.sql.SQLException")))
        Assert.assertEquals("""
    @Override
    public void insertBatch(final java.util.List<com.github.gr3gdev.jdbc.Test> liste) {
        final String sql = "INSERT INTO TEST (ID, NAME, DATE_CREATION, DATE_BIRTHDAY, AVERAGE, ORDER, ACTIVE, DELETED, DOUBLE, TIME_MODIFIED, URL, NUMBER, SHORT, TIME) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (final Connection cnx = SQLDataSource.getConnection("TestDB");
            final PreparedStatement stm = cnx.prepareStatement(sql)) {
            int index = 0;
            for (final com.github.gr3gdev.jdbc.Test element : liste) {
                stm.setLong(1, element.getId());
                stm.setString(2, element.getName());
                stm.setDate(3, element.getDateCreation());
                stm.setDate(4, element.getDateBirthday());
                stm.setFloat(5, element.getAverage());
                stm.setInt(6, element.getOrder());
                stm.setBoolean(7, element.getActive());
                stm.setByte(8, element.getDeleted());
                stm.setDouble(9, element.getDouble());
                stm.setTimestamp(10, element.getTimeModified());
                stm.setURL(11, element.getUrl());
                stm.setBigDecimal(12, element.getNumber());
                stm.setShort(13, element.getShort());
                stm.setTime(14, element.getTime());
                stm.addBatch();
                index++;
                if (index % 1000 == 0 || index == liste.size()) {
                    stm.executeBatch();
                }
            }
        } catch (SQLException throwables) {
            throw new JDBCExecutionException(com.github.gr3gdev.jdbc.dao.QueryType.INSERT, "liste", throwables);
        }
    }
        """.trimIndent(), res.second.trimIndent())
    }

    @Test
    fun testExecuteAutoAttributes() {
        Mockito.`when`(queryName.toString()).thenReturn("insert")

        val res = InsertGenerator(tableElement).execute(query, null, null)
        Assert.assertTrue("Import missing", res.first.containsAll(listOf("java.sql.Connection", "java.sql.PreparedStatement", "java.sql.ResultSet", "java.sql.SQLException")))
        Assert.assertEquals("""
    @Override
    public void insert(final com.github.gr3gdev.jdbc.Test test) {
        final String sql = "INSERT INTO TEST (ID, NAME, DATE_CREATION, DATE_BIRTHDAY, AVERAGE, ORDER, ACTIVE, DELETED, DOUBLE, TIME_MODIFIED, URL, NUMBER, SHORT, TIME) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (final Connection cnx = SQLDataSource.getConnection("TestDB");
            final PreparedStatement stm = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
            stm.executeUpdate();
            try (final ResultSet res = stm.getGeneratedKeys()) {
                if (res.next()) {
                    test.setId(res.getLong("ID"));
                }
            }
        } catch (SQLException throwables) {
            throw new JDBCExecutionException(com.github.gr3gdev.jdbc.dao.QueryType.INSERT, "test", throwables);
        }
    }
        """.trimIndent(), res.second.trimIndent())
    }

    @Test
    fun testExecute() {
        val attributes = columns.map { it.first }

        Mockito.`when`(queryName.toString()).thenReturn("insert")

        val res = InsertGenerator(tableElement).execute(query, attributes, null)
        Assert.assertTrue("Import missing", res.first.containsAll(listOf("java.sql.Connection", "java.sql.PreparedStatement", "java.sql.ResultSet", "java.sql.SQLException")))
        Assert.assertEquals("""
    @Override
    public void insert(final com.github.gr3gdev.jdbc.Test test) {
        final String sql = "INSERT INTO TEST (ID, NAME, DATE_CREATION, DATE_BIRTHDAY, AVERAGE, ORDER, ACTIVE, DELETED, DOUBLE, TIME_MODIFIED, URL, NUMBER, SHORT, TIME) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (final Connection cnx = SQLDataSource.getConnection("TestDB");
            final PreparedStatement stm = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
            stm.executeUpdate();
            try (final ResultSet res = stm.getGeneratedKeys()) {
                if (res.next()) {
                    test.setId(res.getLong("ID"));
                }
            }
        } catch (SQLException throwables) {
            throw new JDBCExecutionException(com.github.gr3gdev.jdbc.dao.QueryType.INSERT, "test", throwables);
        }
    }
        """.trimIndent(), res.second.trimIndent())
    }

}