package com.github.gr3gdev.jdbc

import com.github.gr3gdev.jdbc.error.JDBCExecutionException
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.sql.PreparedStatement
import java.sql.ResultSet

class SQLDataSourceTest {

    @Before
    fun init() {
        SQLDataSource.init("/test.properties", "db")
    }

    @Test
    fun `test - execute invalid`() {
        try {
            SQLDataSource.execute("db", "Not a valid request 1", object : SQLDataSource.StatementExecution {
                override fun run(stm: PreparedStatement) {
                    Assert.fail()
                }
            })
        } catch (exc: JDBCExecutionException) {
            Assert.assertEquals("Error on 'Not a valid request 1'", exc.message)
        }
    }

    @Test
    fun `test - execute`() {
        SQLDataSource.execute("db", "CREATE TABLE T1 (ID LONG PRIMARY KEY)", object : SQLDataSource.StatementExecution {
            override fun run(stm: PreparedStatement) {
                Assert.assertFalse(stm.execute())
            }
        })
    }

    @Test
    fun `test - executeAndGetKey invalid`() {
        try {
            SQLDataSource.executeAndGetKey("db", "Not a valid request 2", object : SQLDataSource.StatementExecution {
                override fun run(stm: PreparedStatement) {
                    Assert.fail()
                }
            }, object : SQLDataSource.ResultSetExecution {
                override fun run(res: ResultSet) {
                    Assert.fail()
                }
            })
        } catch (exc: JDBCExecutionException) {
            Assert.assertEquals("Error on 'Not a valid request 2'", exc.message)
        }
    }

    @Test
    fun `test - executeAndGetKey`() {
        SQLDataSource.execute("db", "CREATE TABLE T2 (ID LONG PRIMARY KEY AUTO_INCREMENT, NAME VARCHAR(10) NOT NULL)", object : SQLDataSource.StatementExecution {
            override fun run(stm: PreparedStatement) {
                Assert.assertFalse(stm.execute())
            }
        })
        SQLDataSource.executeAndGetKey("db", "INSERT INTO T2 (NAME) VALUES (?)", object : SQLDataSource.StatementExecution {
            override fun run(stm: PreparedStatement) {
                stm.setString(1, "test")
                Assert.assertEquals(1, stm.executeUpdate())
            }
        }, object : SQLDataSource.ResultSetExecution {
            override fun run(res: ResultSet) {
                Assert.assertEquals(1, res.getLong("ID"))
            }
        })
    }

    @Test
    fun `test - executeAndUpdate invalid`() {
        try {
            SQLDataSource.executeAndUpdate("db", "Not a valid request 3", object : SQLDataSource.Execution<PreparedStatement, Int> {
                override fun run(obj: PreparedStatement): Int {
                    Assert.fail()
                    return 0
                }
            })
        } catch (exc: JDBCExecutionException) {
            Assert.assertEquals("Error on 'Not a valid request 3'", exc.message)
        }
    }

    @Test
    fun `test - executeAndUpdate`() {
        SQLDataSource.execute("db", "CREATE TABLE T3 (ID LONG PRIMARY KEY AUTO_INCREMENT, NAME VARCHAR(10) NOT NULL)", object : SQLDataSource.StatementExecution {
            override fun run(stm: PreparedStatement) {
                Assert.assertFalse(stm.execute())
            }
        })
        SQLDataSource.execute("db", "INSERT INTO T3 (NAME) VALUES (?),(?)", object : SQLDataSource.StatementExecution {
            override fun run(stm: PreparedStatement) {
                stm.setString(1, "n1")
                stm.setString(2, "n2")
                stm.executeUpdate()
            }
        })
        val maj = SQLDataSource.executeAndUpdate("db", "UPDATE T3 SET NAME = ? WHERE ID = ?", object : SQLDataSource.Execution<PreparedStatement, Int> {
            override fun run(obj: PreparedStatement): Int {
                obj.setString(1, "test")
                obj.setLong(2, 1)
                return obj.executeUpdate()
            }
        })
        Assert.assertEquals(1, maj)
    }

    @Test
    fun `test - executeAndReturn invalid`() {
        try {
            SQLDataSource.executeAndReturn("db", "Not a valid request 4", object : SQLDataSource.StatementExecution {
                override fun run(stm: PreparedStatement) {
                    Assert.fail()
                }
            }, object : SQLDataSource.Execution<ResultSet, Int> {
                override fun run(obj: ResultSet): Int {
                    Assert.fail()
                    return 0
                }

            })
        } catch (exc: JDBCExecutionException) {
            Assert.assertEquals("Error on 'Not a valid request 4'", exc.message)
        }
    }

    @Test
    fun `test - executeAndReturn`() {
        SQLDataSource.execute("db", "CREATE TABLE T4 (ID LONG PRIMARY KEY AUTO_INCREMENT, NAME VARCHAR(10) NOT NULL)", object : SQLDataSource.StatementExecution {
            override fun run(stm: PreparedStatement) {
                Assert.assertFalse(stm.execute())
            }
        })
        SQLDataSource.execute("db", "INSERT INTO T4 (NAME) VALUES (?),(?),(?)", object : SQLDataSource.StatementExecution {
            override fun run(stm: PreparedStatement) {
                stm.setString(1, "r1")
                stm.setString(2, "r2")
                stm.setString(3, "r3")
                stm.executeUpdate()
            }
        })
        val list = SQLDataSource.executeAndReturn("db", "SELECT NAME FROM T4 ORDER BY ID DESC", object : SQLDataSource.StatementExecution {
            override fun run(stm: PreparedStatement) {
                // None
            }
        }, object : SQLDataSource.Execution<ResultSet, List<String>> {
            override fun run(obj: ResultSet): List<String> {
                val res = ArrayList<String>()
                while (obj.next()) {
                    res.add(obj.getString("NAME"))
                }
                return res
            }

        })
        Assert.assertEquals(3, list.size)
        Assert.assertEquals("r3", list[0])
        Assert.assertEquals("r2", list[1])
        Assert.assertEquals("r1", list[2])
    }
}