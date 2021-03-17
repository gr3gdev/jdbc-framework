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

    private lateinit var folder: File
    private lateinit var dataSource: HikariDataSource

    @Before
    fun `init datasource`() {
        val classpathFolder = File(UpgradeVersion::class.java.getResource("/").toURI())
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
    fun `test upgrade - database folder does not exists`() {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:h2:mem:test3"
        val dataSource2 = HikariDataSource(config)

        UpgradeVersion.createSchemaVersion(dataSource2, "test3")

        assertNull(UpgradeVersion.parseUpgradeFiles(dataSource2, "test3"))
    }

    @Test
    fun `test upgrade - script without version folder`() {
        val version = File(folder, "NotVersion")
        version.mkdirs()
        val script = File(version, "script_1.sql")
        script.writeText("SELECT 1")

        try {
            UpgradeVersion.parseUpgradeFiles(dataSource, "test")
            fail("Exception expected")
        } catch (exc: RuntimeException) {
            assertEquals("SQL file 'script_1.sql' must be under a version directory : Vx.y.z (ex: V1.0.0) => [NotVersion]", exc.message)
        }
    }

    @Test
    fun `test successful upgrade`() {
        val version1 = File(folder, "V1.0.0")
        version1.mkdirs()
        val script = File(version1, "script_1.sql")
        script.writeText("SELECT 1")

        val md = MessageDigest.getInstance("MD5")
        val checksum = BigInteger(1, md.digest(script.readBytes())).toString(16).padStart(32, '0')

        val list = UpgradeVersion.parseUpgradeFiles(dataSource, "test")
        assertNotNull(list!!)
        assertEquals(1, list.size)
        assertEquals("V1.0.0", list[0].version)
        assertEquals("1", list[0].part)
        assertEquals("script_1.sql", list[0].file)
        assertEquals(checksum, list[0].checksum)
        assertEquals("1", list[0].status)
    }

    @Test
    fun `test upgrade multiple`() {
        val version1 = File(folder, "V1.0.0")
        version1.mkdirs()
        val script1OK = File(version1, "script_1.sql")
        script1OK.writeText("SELECT 1")
        val script2KO = File(version1, "script_2.sql")
        script2KO.writeText("ERROR")

        val version2 = File(folder, "V1.1.0")
        version2.mkdirs()
        val script3OK = File(version2, "script_3.sql")
        script3OK.writeText("CREATE TABLE T1 (ID INT PRIMARY KEY)")

        val md = MessageDigest.getInstance("MD5")
        val checksumScript1 = BigInteger(1, md.digest(script1OK.readBytes())).toString(16).padStart(32, '0')
        val checksumScript2 = BigInteger(1, md.digest(script2KO.readBytes())).toString(16).padStart(32, '0')
        val checksumScript3 = BigInteger(1, md.digest(script3OK.readBytes())).toString(16).padStart(32, '0')

        var list = UpgradeVersion.parseUpgradeFiles(dataSource, "test")
        assertNotNull(list!!)
        assertEquals(3, list.size)

        assertEquals("V1.0.0", list[0].version)
        assertEquals("V1.0.0", list[1].version)
        assertEquals("V1.1.0", list[2].version)

        assertEquals("1", list[0].part)
        assertEquals("2", list[1].part)
        assertEquals("1", list[2].part)

        assertEquals("script_1.sql", list[0].file)
        assertEquals("script_2.sql", list[1].file)
        assertEquals("script_3.sql", list[2].file)

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
        val version1 = File(folder, "V1.0.0")
        version1.mkdirs()
        val script = File(version1, "script_1.sql")
        script.writeText("CREATE TABLE T1 (ID INT, NAME TEXT)")

        val md = MessageDigest.getInstance("MD5")
        val checksum = BigInteger(1, md.digest(script.readBytes())).toString(16).padStart(32, '0')

        val list = UpgradeVersion.parseUpgradeFiles(dataSource, "test")
        assertNotNull(list!!)
        assertEquals(1, list.size)
        assertEquals("V1.0.0", list[0].version)
        assertEquals("1", list[0].part)
        assertEquals("script_1.sql", list[0].file)
        assertEquals(checksum, list[0].checksum)
        assertEquals("1", list[0].status)

        script.writeText("CREATE TABLE T1 (ID INT PRIMARY KEY, NAME TEXT)")

        try {
            UpgradeVersion.parseUpgradeFiles(dataSource, "test")
            fail("Exception expected")
        } catch (exc: RuntimeException) {
            assertEquals("Content of file 'script_1.sql' has changed !", exc.message)
        }
    }

    @Test
    fun `test upgrade order change`() {
        val version1 = File(folder, "V1.0.0")
        version1.mkdirs()
        val script = File(version1, "script_2.sql")
        script.writeText("CREATE TABLE T1 (ID INT PRIMARY KEY, NAME TEXT)")

        val md = MessageDigest.getInstance("MD5")
        val checksum = BigInteger(1, md.digest(script.readBytes())).toString(16).padStart(32, '0')

        val list = UpgradeVersion.parseUpgradeFiles(dataSource, "test")
        assertNotNull(list!!)
        assertEquals(1, list.size)
        assertEquals("V1.0.0", list[0].version)
        assertEquals("1", list[0].part)
        assertEquals("script_2.sql", list[0].file)
        assertEquals(checksum, list[0].checksum)
        assertEquals("1", list[0].status)

        val script1 = File(version1, "script_1.sql")
        script1.writeText("SELECT 1")

        try {
            UpgradeVersion.parseUpgradeFiles(dataSource, "test")
            fail("Exception expected")
        } catch (exc: RuntimeException) {
            assertEquals("Order of file 'script_2.sql' has changed !", exc.message)
        }
    }

}