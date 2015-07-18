package org.ops4j.pax.jdbc.hsqldb.impl;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.hsqldb.jdbc.JDBCCommonDataSource;
import org.hsqldb.jdbc.JDBCDataSource;
import org.hsqldb.jdbc.JDBCDriver;
import org.osgi.service.jdbc.DataSourceFactory;

public class HsqldbDataSourceFactory implements DataSourceFactory {

    @Override
    public DataSource createDataSource(Properties props) throws SQLException {
        JDBCDataSource ds = new JDBCDataSource();
        setProperties(ds, props);
        return ds;
    }

    private void setProperties(JDBCCommonDataSource ds, Properties properties) throws SQLException {
        Properties props = (Properties) properties.clone();
        String url = (String) props.remove(DataSourceFactory.JDBC_URL);
        if (url != null) {
            ds.setUrl(url);
        }
        String databaseName = (String) props.remove(DataSourceFactory.JDBC_DATABASE_NAME);
        if (databaseName == null && url == null) {
            throw new SQLException("missing required property "
                + DataSourceFactory.JDBC_DATABASE_NAME);
        }
        ds.setDatabaseName(databaseName);

        if (props.containsKey(DataSourceFactory.JDBC_PASSWORD)) {
            String password = (String) props.remove(DataSourceFactory.JDBC_PASSWORD);
            ds.setPassword(password);
        }

        String user = (String) props.remove(DataSourceFactory.JDBC_USER);
        ds.setUser(user);

        if (!props.isEmpty()) {
            throw new SQLException("cannot set properties " + props.keySet());
        }
    }

    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource(Properties props)
        throws SQLException {
        PoolDataSourceWrapper ds = new PoolDataSourceWrapper();
        setProperties(ds, props);

        return ds;
    }

    @Override
    public XADataSource createXADataSource(Properties props) throws SQLException {
        throw new SQLException("XADataSource not supported");
    }

    @Override
    public Driver createDriver(Properties props) throws SQLException {
        JDBCDriver driver = new JDBCDriver();
        return driver;
    }
}
