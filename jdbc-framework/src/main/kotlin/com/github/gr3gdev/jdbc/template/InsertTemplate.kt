package com.github.gr3gdev.jdbc.template

internal object InsertTemplate {

    fun generate(methodName: String, parameters: String, databaseName: String, sql: String, setters: String, classType: String, parameterName: String): String {
        return """
    @Override
    public void $methodName($parameters) {
        final String sql = "$sql";
        SQLDataSource.execute("$databaseName", sql, (stm) -> {
            int index = 0;
            for (final $classType element : $parameterName) {
    $setters
                stm.addBatch();
                index++;
                if (index % 1000 == 0 || index == $parameterName.size()) {
                    stm.executeBatch();
                }
            }
        });
    }"""
    }

    fun generate(methodName: String, parameters: String, databaseName: String, sql: String, setters: String): String {
        return """
    @Override
    public void $methodName($parameters) {
        final String sql = "$sql";
        SQLDataSource.execute("$databaseName", sql, (stm) -> {
    $setters
            stm.executeUpdate();
        });
    }"""
    }

    fun generate(methodName: String, parameters: String, databaseName: String, sql: String, setters: String, setID: String): String {
        return """
    @Override
    public void $methodName($parameters) {
        final String sql = "$sql";
        SQLDataSource.executeAndGetKey("$databaseName", sql, (stm) -> {
    $setters
            stm.executeUpdate();
        }, (res) -> {
    $setID
        });
    }"""
    }

}