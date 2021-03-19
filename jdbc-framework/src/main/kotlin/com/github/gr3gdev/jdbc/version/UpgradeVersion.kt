package com.github.gr3gdev.jdbc.version

import com.zaxxer.hikari.HikariDataSource
import java.io.File
import java.math.BigInteger
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.sql.SQLException

internal object UpgradeVersion {

    fun createSchemaVersion(hikariDataSource: HikariDataSource, databaseName: String) {
        val databaseResource = getDatabaseFolder(databaseName)
        if (databaseResource != null) {
            hikariDataSource.connection.use { cnx ->
                cnx.metaData.getTables(null, null, "SCHEMA_VERSION", null).use { res ->
                    if (!res.next()) {
                        cnx.createStatement().use { stm ->
                            stm.execute("""
CREATE TABLE SCHEMA_VERSION (
  VERSION  varchar(12),
  PART     char(2),
  FILE     varchar(255),
  CHECKSUM varchar(255),
  STATUS   char(1),
  PRIMARY KEY (VERSION, PART)
)
            """.trimIndent())
                        }
                    }
                }
            }
        }
    }

    internal class SchemaVersion(val version: String, val part: String, val file: String, var checksum: String, val status: String)

    private fun getDatabaseFolder(databaseName: String): URL? {
        return UpgradeVersion::class.java.getResource("/$databaseName")
    }

    private fun getListVersions(hikariDataSource: HikariDataSource): List<SchemaVersion> {
        return hikariDataSource.connection.use { cnx ->
            cnx.createStatement().use { stm ->
                stm.executeQuery("SELECT VERSION, PART, FILE, CHECKSUM, STATUS FROM SCHEMA_VERSION").use { res ->
                    val list = ArrayList<SchemaVersion>()
                    while (res.next()) {
                        list.add(SchemaVersion(
                                res.getString(1),
                                res.getString(2),
                                res.getString(3),
                                res.getString(4),
                                res.getString(5)
                        ))
                    }
                    list
                }
            }
        }
    }

    fun parseUpgradeFiles(hikariDataSource: HikariDataSource, databaseName: String): List<SchemaVersion>? {
        val databaseResource = getDatabaseFolder(databaseName)
        if (databaseResource != null) {
            var lastVersion: String? = null
            var part = 1
            val insertSql = "INSERT INTO SCHEMA_VERSION (VERSION, PART, FILE, CHECKSUM, STATUS) VALUES (?, ?, ?, ?, ?)"
            val updateSql = "UPDATE SCHEMA_VERSION SET CHECKSUM = ?, STATUS = ? WHERE VERSION = ? AND PART = ? AND FILE = ?"
            val md = MessageDigest.getInstance("MD5")
            val listVersion = getListVersions(hikariDataSource)
            File(databaseResource.file)
                    .walk(FileWalkDirection.TOP_DOWN)
                    .filter { it.extension == "sql" }
                    .sortedBy {
                        it.absolutePath
                    }
                    .forEach {
                        val version = it.parentFile.name
                        if (lastVersion != null && lastVersion != version) {
                            part = 1
                        }
                        if (!version.matches(Regex("V[0-9]+\\.[0-9]+\\.[0-9]+"))) {
                            throw RuntimeException("SQL file '${it.name}' must be under a version directory : Vx.y.z (ex: V1.0.0) => [$version]")
                        }
                        val checksum = BigInteger(1, md.digest(it.readBytes())).toString(16).padStart(32, '0')
                        val alreadyExecute = listVersion.firstOrNull { v -> v.version == version && v.part == part.toString() }
                        if (alreadyExecute != null && alreadyExecute.status == "1") {
                            if (alreadyExecute.file != it.name) {
                                throw RuntimeException("Order of file '${alreadyExecute.file}' has changed !")
                            }
                            if (alreadyExecute.checksum != checksum) {
                                throw RuntimeException("Content of file '${it.name}' has changed !")
                            }
                        } else {
                            hikariDataSource.connection.use { cnx ->
                                var status = "1"
                                try {
                                    cnx.createStatement().use { stm ->
                                        stm.execute(it.readText(StandardCharsets.UTF_8))
                                    }
                                } catch (exc: SQLException) {
                                    exc.printStackTrace()
                                    status = "0"
                                }
                                if (alreadyExecute != null && alreadyExecute.status == "0") {
                                    cnx.prepareStatement(updateSql).use { stm ->
                                        stm.setString(1, checksum)
                                        stm.setString(2, status)
                                        stm.setString(3, version)
                                        stm.setString(4, part.toString())
                                        stm.setString(5, it.name)
                                        stm.execute()
                                    }
                                } else {
                                    cnx.prepareStatement(insertSql).use { stm ->
                                        stm.setString(1, version)
                                        stm.setString(2, part.toString())
                                        stm.setString(3, it.name)
                                        stm.setString(4, checksum)
                                        stm.setString(5, status)
                                        stm.execute()
                                    }
                                }
                            }
                        }
                        part++
                        lastVersion = version
                    }
            return getListVersions(hikariDataSource)
        } else {
            return null
        }
    }

}