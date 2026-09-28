package vn.quanlybanhang.dao;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import vn.quanlybanhang.config.DatabaseConfig;

public abstract class BaseDao {

    private final DataSource dataSource;

    protected BaseDao() {
        this.dataSource = DatabaseConfig.getDataSource();
    }

    protected Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
