package com.github.gr3gdev.jdbc.version

import com.zaxxer.hikari.HikariDataSource
import java.io.InputStream
import java.io.InputStreamReader
import java.math.BigInteger
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.sql.Connection
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

internal object UpgradeVersion {

    fun createSchemaVersion(hikariDataSource: HikariDataSource, databaseName: String) {
        val databaseResource = getDatabaseProperties(databaseName)
        if (databaseResource != null) {
            hikariDataSource.connection.use { cnx ->
                cnx.metaData.getTables(null, null, "SCHEMA_VERSION", null)
                        .use { res ->
                            if (!res.next()) {
                                cnx.metaData.getTables(null, null, "schema_version", null)
                                        .use { res2 ->
                                            if (!res2.next()) {
                                                addTableSchemaVersion(cnx)
                                            }
                                        }
                            }
                        }
            }
        }
    }

    private fun addTableSchemaVersion(cnx: Connection) {
        cnx.createStatement().use { stm ->
            stm.execute("""
CREATE TABLE SCHEMA_VERSION (
  VERSION     varchar(12),
  PART        char(2),
  DESCRIPTION varchar(255),
  CHECKSUM    varchar(255),
  STATUS      char(1),
  PRIMARY KEY (VERSION, PART)
)
            """.trimIndent())
        }
    }

    internal class SchemaVersion(val version: String, val order: String, val description: String, var checksum: String, val status: String)

    private fun getDatabaseProperties(databaseName: String): URL? {
        return UpgradeVersion::class.java.getResource("/$databaseName.properties")
    }

    private fun getListVersions(hikariDataSource: HikariDataSource): List<SchemaVersion> {
        return hikariDataSource.connection.use { cnx ->
            cnx.createStatement().use { stm ->
                stm.executeQuery("SELECT VERSION, PART, DESCRIPTION, CHECKSUM, STATUS FROM SCHEMA_VERSION").use { res ->
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

    internal class Script(val inputStream: InputStream, val version: String, val description: String, val order: String)

    fun parseUpgradeFiles(hikariDataSource: HikariDataSource, databaseName: String): List<SchemaVersion>? {
        val databaseResource = getDatabaseProperties(databaseName)
        if (databaseResource != null) {
            val insertSql = "INSERT INTO SCHEMA_VERSION (VERSION, PART, DESCRIPTION, CHECKSUM, STATUS) VALUES (?, ?, ?, ?, ?)"
            val selectSql = "SELECT VERSION, PART, DESCRIPTION, CHECKSUM, STATUS FROM SCHEMA_VERSION WHERE VERSION = ? AND PART = ?"
            val updateSql = "UPDATE SCHEMA_VERSION SET CHECKSUM = ?, STATUS = ? WHERE VERSION = ? AND PART = ?"
            val md = MessageDigest.getInstance("MD5")
            val properties = Properties()
            properties.load(UpgradeVersion::class.java.getResourceAsStream("/$databaseName.properties"))
            val scripts = properties.stringPropertyNames().map {
                // Format: vX_Y_Z.description = path_sql_file
                if (!it.matches(Regex("v[0-9]+_[0-9]+_[0-9]+\\.[_a-zA-Z0-9]+\\.[0-9]+"))) {
                    throw RuntimeException("Property file must contains key : vX_Y_Z.alphanumeric_description.order [invalid '$it']")
                }
                val inputStream = UpgradeVersion::class.java.getResourceAsStream(properties[it] as String)
                        ?: throw RuntimeException("File ${properties[it]} does not exists !")
                val version = it.split(".")[0].replace("_", ".")
                val description = it.split(".")[1].replace("_", " ")
                val order = it.split(".")[2]
                Script(inputStream, version, description, order)
            }
            scripts.sortedWith(Comparator.comparing(Script::version).thenComparing(Script::order))
                    .forEach {
                        it.inputStream.use { stream ->
                            val sqlContent = InputStreamReader(stream).readLines().joinToString("\n")
                            val version = it.version
                            val description = it.description
                            val order = it.order
                            val checksum = BigInteger(1, md.digest(sqlContent.toByteArray(StandardCharsets.UTF_8))).toString(16).padStart(32, '0')
                            val alreadyExecute  = hikariDataSource.connection.use { cnx ->
                                cnx.prepareStatement(selectSql).use { stm ->
                                    stm.setString(1, version)
                                    stm.setString(2, order)
                                    stm.executeQuery().use { res ->
                                        if (res.next()) {
                                            SchemaVersion(
                                                    res.getString(1),
                                                    res.getString(2),
                                                    res.getString(3),
                                                    res.getString(4),
                                                    res.getString(5))
                                        } else {
                                            null
                                        }
                                    }
                                }
                            }
                            if (alreadyExecute != null && alreadyExecute.status == "1") {
                                if (alreadyExecute.description != description) {
                                    throw RuntimeException("Order of '${alreadyExecute.description}' has changed !")
                                }
                                if (alreadyExecute.checksum != checksum) {
                                    throw RuntimeException("Content of '${alreadyExecute.description}' has changed !")
                                }
                            } else {
                                hikariDataSource.connection.use { cnx ->
                                    var status = "1"
                                    try {
                                        cnx.createStatement().use { stm ->
                                            stm.execute(sqlContent)
                                        }
                                    } catch (exc: Exception) {
                                        exc.printStackTrace()
                                        status = "0"
                                    }
                                    if (alreadyExecute != null && alreadyExecute.status == "0") {
                                        cnx.prepareStatement(updateSql).use { stm ->
                                            stm.setString(1, checksum)
                                            stm.setString(2, status)
                                            stm.setString(3, version)
                                            stm.setString(4, order)
                                            stm.execute()
                                        }
                                    } else {
                                        cnx.prepareStatement(insertSql).use { stm ->
                                            stm.setString(1, version)
                                            stm.setString(2, order)
                                            stm.setString(3, description)
                                            stm.setString(4, checksum)
                                            stm.setString(5, status)
                                            stm.execute()
                                        }
                                    }
                                }
                            }
                        }
                    }
            return getListVersions(hikariDataSource)
        } else {
            return null
        }
    }

}