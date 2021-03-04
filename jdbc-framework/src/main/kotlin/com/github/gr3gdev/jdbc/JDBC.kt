package com.github.gr3gdev.jdbc

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class JDBC(
        val conf: Array<JdbcConf>
)