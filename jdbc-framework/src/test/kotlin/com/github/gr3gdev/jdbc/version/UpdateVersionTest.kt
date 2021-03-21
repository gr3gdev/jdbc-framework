package com.github.gr3gdev.jdbc.version

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

class UpdateVersionTest {

    private lateinit var databaseFile: File
    private lateinit var folder: File
    private lateinit var dataSource: HikariDataSource

    @Before
    fun `init datasource`() {
        val classpathFolder = File(UpgradeVersion::class.java.getResource("/").toURI())
        databaseFile = File(classpathFolder, "test.properties")
        folder = File(classpathFolder, "test")
        folder.mkdirs()

        val config = HikariConfig()
        config.jdbcUrl = "jdbc:h2:mem:test"
        dataSource = HikariDataSource(config)
        UpgradeVersion.createSchemaVersion(dataSource, "test")
    }

    @After
    fun `delete database folder`() {
        dataSource.close()
        folder.deleteRecursively()
        databaseFile.deleteRecursively()
    }

    @Test
    fun `test create table SCHEMA_VERSION`() {
        dataSource.connection.use { cnx ->
            cnx.metaData.getTables(null, null, "SCHEMA_VERSION", null).use { res ->
                assertTrue("La table SCHEMA_VERSION n'existe pas", res.next())
            }
        }
    }

    @Test
    fun `test don't create table SCHEMA_VERSION`() {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:h2:mem:test2"
        val dataSource2 = HikariDataSource(config)

        UpgradeVersion.createSchemaVersion(dataSource2, "test2")

        dataSource2.connection.use { cnx ->
            cnx.metaData.getTables(null, null, "SCHEMA_VERSION", null).use { res ->
                assertFalse("La table SCHEMA_VERSION existe", res.next())
            }
        }
    }

    @Test
    fun `test upgrade - database file does not exists`() {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:h2:mem:test3"
        val dataSource2 = HikariDataSource(config)

        UpgradeVersion.createSchemaVersion(dataSource2, "test3")

        assertNull(UpgradeVersion.parseUpgradeFiles(dataSource2, "test3"))
    }

    @Test
    fun `test upgrade - script invalid version`() {
        databaseFile.writeText("""
V2.45.test.1=/test/script_1.sql
v1.0.description=/test/script_2.sql
        """.trimIndent())

        try {
            UpgradeVersion.parseUpgradeFiles(dataSource, "test")
            fail("Exception expected")
        } catch (exc: RuntimeException) {
            assertEquals("Property file must contains key : vX_Y_Z.alphanumeric_description.order [invalid 'v1.0.description']", exc.message)
        }
    }

    @Test
    fun `test upgrade - script file does not exists`() {
        databaseFile.writeText("v1_0_1.test.1=/test/script_1.sql")

        try {
            UpgradeVersion.parseUpgradeFiles(dataSource, "test")
            fail("Exception expected")
        } catch (exc: RuntimeException) {
            assertEquals("File /test/script_1.sql does not exists !", exc.message)
        }
    }

    @Test
    fun `test successful upgrade`() {
        databaseFile.writeText("v1_0_0.description_de_test.1=/test/script_1.sql")
        val script = File(folder, "script_1.sql")
        script.writeText("SELECT 1")

        val md = MessageDigest.getInstance("MD5")
        val checksum = BigInteger(1, md.digest(script.readBytes())).toString(16).padStart(32, '0')

        val list = UpgradeVersion.parseUpgradeFiles(dataSource, "test")
        assertNotNull(list!!)
        assertEquals(1, list.size)
        assertEquals("v1.0.0", list[0].version)
        assertEquals("1", list[0].order)
        assertEquals("description de test", list[0].description)
        assertEquals(checksum, list[0].checksum)
        assertEquals("1", list[0].status)
    }

    @Test
    fun `test upgrade multiple`() {
        databaseFile.writeText("""
v1_0_0.description_pour_script_1.1=/test/script_1.sql
v1_0_0.description_pour_script_2.2=/test/script_2.sql
v1_1_0.version_suivante.1=/test/script_3.sql
        """.trimIndent())
        val script1OK = File(folder, "script_1.sql")
        script1OK.writeText("SELECT 1")
        val script2KO = File(folder, "script_2.sql")
        script2KO.writeText("ERROR")
        val script3OK = File(folder, "script_3.sql")
        script3OK.writeText("CREATE TABLE T1 (ID INT PRIMARY KEY)")

        val md = MessageDigest.getInstance("MD5")
        val checksumScript1 = BigInteger(1, md.digest(script1OK.readBytes())).toString(16).padStart(32, '0')
        val checksumScript2 = BigInteger(1, md.digest(script2KO.readBytes())).toString(16).padStart(32, '0')
        val checksumScript3 = BigInteger(1, md.digest(script3OK.readBytes())).toString(16).padStart(32, '0')

        var list = UpgradeVersion.parseUpgradeFiles(dataSource, "test")
        assertNotNull(list!!)
        assertEquals(3, list.size)

        assertEquals("v1.0.0", list[0].version)
        assertEquals("v1.0.0", list[1].version)
        assertEquals("v1.1.0", list[2].version)

        assertEquals("1", list[0].order)
        assertEquals("2", list[1].order)
        assertEquals("1", list[2].order)

        assertEquals("description pour script 1", list[0].description)
        assertEquals("description pour script 2", list[1].description)
        assertEquals("version suivante", list[2].description)

        assertEquals(checksumScript1, list[0].checksum)
        assertEquals(checksumScript2, list[1].checksum)
        assertEquals(checksumScript3, list[2].checksum)

        assertEquals("1", list[0].status)
        assertEquals("0", list[1].status)
        assertEquals("1", list[2].status)

        script2KO.writeText("CREATE TABLE T0 (ID LONG PRIMARY KEY)")

        list = UpgradeVersion.parseUpgradeFiles(dataSource, "test")
        assertNotNull(list!!)
        assertEquals(3, list.size)

        val newChecksumScript2 = BigInteger(1, md.digest(script2KO.readBytes())).toString(16).padStart(32, '0')
        assertEquals(newChecksumScript2, list[1].checksum)
        assertEquals("1", list[1].status)
    }

    @Test
    fun `test upgrade checksum change`() {
        databaseFile.writeText("""
v1_0_0.test_checksum.1=/test/script_1.sql
        """.trimIndent())
        val script = File(folder, "script_1.sql")
        script.writeText("CREATE TABLE T1 (ID INT, NAME TEXT)")

        val md = MessageDigest.getInstance("MD5")
        val checksum = BigInteger(1, md.digest(script.readBytes())).toString(16).padStart(32, '0')

        val list = UpgradeVersion.parseUpgradeFiles(dataSource, "test")
        assertNotNull(list!!)
        assertEquals(1, list.size)
        assertEquals("v1.0.0", list[0].version)
        assertEquals("1", list[0].order)
        assertEquals("test checksum", list[0].description)
        assertEquals(checksum, list[0].checksum)
        assertEquals("1", list[0].status)

        script.writeText("CREATE TABLE T1 (ID INT PRIMARY KEY, NAME TEXT)")

        try {
            UpgradeVersion.parseUpgradeFiles(dataSource, "test")
            fail("Exception expected")
        } catch (exc: RuntimeException) {
            assertEquals("Content of 'test checksum' has changed !", exc.message)
        }
    }

    @Test
    fun `test upgrade order change`() {
        databaseFile.writeText("""
v1_0_0.test.1=/test/script_2.sql
        """.trimIndent())
        val script = File(folder, "script_2.sql")
        script.writeText("CREATE TABLE T1 (ID INT PRIMARY KEY, NAME TEXT)")

        val md = MessageDigest.getInstance("MD5")
        val checksum = BigInteger(1, md.digest(script.readBytes())).toString(16).padStart(32, '0')

        val list = UpgradeVersion.parseUpgradeFiles(dataSource, "test")
        assertNotNull(list!!)
        assertEquals(1, list.size)
        assertEquals("v1.0.0", list[0].version)
        assertEquals("1", list[0].order)
        assertEquals("test", list[0].description)
        assertEquals(checksum, list[0].checksum)
        assertEquals("1", list[0].status)

        databaseFile.writeText("""
v1_0_0.test1.1=/test/script_1.sql
v1_0_0.test2.2=/test/script_2.sql
        """.trimIndent())
        val script1 = File(folder, "script_1.sql")
        script1.writeText("SELECT 1")

        try {
            UpgradeVersion.parseUpgradeFiles(dataSource, "test")
            fail("Exception expected")
        } catch (exc: RuntimeException) {
            assertEquals("Order of 'test' has changed !", exc.message)
        }
    }

}