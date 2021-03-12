package com.github.gr3gdev.jdbc

@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class JdbcConf(
        val configFile: String,
        val databaseName: String = "default",
        val autoincrementSyntax: String = "AUTO_INCREMENT"
)