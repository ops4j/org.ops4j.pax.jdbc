package org.ops4j.pax.jdbc.hsqldb.impl;

import java.sql.SQLException;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

import org.hsqldb.jdbc.JDBCPool;

public class PoolDataSourceWrapper extends JDBCPool implements ConnectionPoolDataSource {

    private static final long serialVersionUID = -2293621933197058388L;

    @Override
    public PooledConnection getPooledConnection() throws SQLException {
        return this.getPooledConnection();
    }

    @Override
    public PooledConnection getPooledConnection(String user, String password) throws SQLException {
        return this.getPooledConnection(user, password);
    }

}
