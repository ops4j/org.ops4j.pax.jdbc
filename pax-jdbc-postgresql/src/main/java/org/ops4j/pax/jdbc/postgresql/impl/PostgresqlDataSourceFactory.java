/*
 * Copyright 2012 Harald Wellmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.jdbc.postgresql.impl;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.osgi.service.jdbc.DataSourceFactory;
import org.postgresql.ds.PGConnectionPoolDataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.postgresql.ds.common.BaseDataSource;
import org.postgresql.xa.PGXADataSource;

public class PostgresqlDataSourceFactory implements DataSourceFactory {

    @Override
    public DataSource createDataSource(Properties props) throws SQLException {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        setProperties(ds, props);
        return ds;
    }

    private void setProperties(BaseDataSource ds, Properties properties) throws SQLException {
        Properties props = (Properties) properties.clone();
        String databaseName = (String) props.remove(DataSourceFactory.JDBC_DATABASE_NAME);
        if (databaseName == null) {
            throw new SQLException("missing required property "
                + DataSourceFactory.JDBC_DATABASE_NAME);
        }
        ds.setDatabaseName(databaseName);

        String password = (String) props.remove(DataSourceFactory.JDBC_PASSWORD);
        ds.setPassword(password);

        String portNumber = (String) props.remove(DataSourceFactory.JDBC_PORT_NUMBER);
        if (portNumber != null) {
            ds.setPortNumber(Integer.parseInt(portNumber));
        }

        String serverName = (String) props.remove(DataSourceFactory.JDBC_SERVER_NAME);
        ds.setServerName(serverName);

        String user = (String) props.remove(DataSourceFactory.JDBC_USER);
        ds.setUser(user);

        if (!props.isEmpty()) {
            throw new SQLException("cannot set properties " + props.keySet());
        }
    }

    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource(Properties props)
        throws SQLException {
        PGConnectionPoolDataSource ds = new PGConnectionPoolDataSource();
        setProperties(ds, props);
        return ds;
    }

    @Override
    public XADataSource createXADataSource(Properties props) throws SQLException {
        PGXADataSource ds = new PGXADataSource();
        setProperties(ds, props);
        return ds;
    }

    @Override
    public Driver createDriver(Properties props) throws SQLException {
        org.postgresql.Driver driver = new org.postgresql.Driver();
        return driver;
    }

}
