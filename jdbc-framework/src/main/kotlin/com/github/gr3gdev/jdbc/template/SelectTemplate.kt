package com.github.gr3gdev.jdbc.template

internal object SelectTemplate {

    fun generate(methodName: String, parameters: String, databaseName: String, sql: String, setters: String, returnType: String,
                 mapAttributes: String, collectionInstance: String): String {
        return """
    @Override
    public $returnType $methodName($parameters) {
        final $returnType ret = $collectionInstance;
        final String sql = "$sql";
        return SQLDataSource.executeAndReturn("$databaseName", sql, (stm) -> {
    $setters
        }, (res) -> {
            while (res.next()) {
    $mapAttributes
            }
            return ret;
        });
    }"""
    }

    fun generate(methodName: String, parameters: String, databaseName: String, sql: String, setters: String, returnType: String,
                 mapAttributes: String): String {
        return """
    @Override
    public $returnType $methodName($parameters) {
        final String sql = "$sql";
        return SQLDataSource.executeAndReturn("$databaseName", sql, (stm) -> {
    $setters
        }, (res) -> {
            if (res.next()) {
    $mapAttributes
            }
            return java.util.Optional.empty();
        });
    }"""
    }

}