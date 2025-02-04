package com.manv.db_explorer_app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DatabaseService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<String> getTableNames() throws SQLException {
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
        List<String> tableNames = new ArrayList<>();

        while (tables.next()) {
            tableNames.add(tables.getString("TABLE_NAME"));
        }

        tables.close();
        connection.close();
        return tableNames;
    }

    public List<Map<String, Object>> getDataFromTable(String tableName) {
        String sql = "SELECT * FROM " + tableName;
        return jdbcTemplate.queryForList(sql);
    }

    public void updateData(String tableName, Map<String, Object> updatedRow, Map<String, Object> originalRow) {
        StringBuilder sql = new StringBuilder("UPDATE " + tableName + " SET ");
        List<Object> params = new ArrayList<>();

        for (String column : updatedRow.keySet()) {
            if (!column.equals("id")) { // Предполагаем, что 'id' - это первичный ключ
                sql.append(column).append(" = ?, ");
                params.add(updatedRow.get(column));
            }
        }

        sql.setLength(sql.length() - 2); // Удаляем последнюю запятую
        sql.append(" WHERE id = ?");
        params.add(originalRow.get("id"));

        jdbcTemplate.update(sql.toString(), params.toArray());
    }
}