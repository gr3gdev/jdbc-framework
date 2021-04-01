package com.github.gr3gdev.jdbc.template

internal object DeleteOrUpdateTemplate {

    fun generate(methodName: String, parameters: String, databaseName: String, sql: String, setters: String) = """
    @Override
    public int $methodName($parameters) {
        final String sql = "$sql";
        return SQLDataSource.executeAndUpdate("$databaseName", sql, (stm) -> {
    $setters
            return stm.executeUpdate();
        });
    }"""

}