/*
 * Copyright 2021 OPS4J.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.jdbc.oracle.impl;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import oracle.jdbc.OracleDriver;
import oracle.jdbc.pool.OracleConnectionPoolDataSource;
import oracle.jdbc.pool.OracleDataSource;
import oracle.jdbc.xa.client.OracleXADataSource;
import org.ops4j.pax.jdbc.common.BeanConfig;
import org.osgi.service.jdbc.DataSourceFactory;

public class OracleDataSourceFactory implements DataSourceFactory {

    @Override
    public DataSource createDataSource(Properties props) throws SQLException {
        try {
            OracleDataSource ds = new OracleDataSource();
            setProperties(ds, props);
            return ds;
        }
        catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource(Properties props)
        throws SQLException {
        try {
            OracleConnectionPoolDataSource ds = new OracleConnectionPoolDataSource();
            setProperties(ds, props);
            return ds;
        }
        catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public XADataSource createXADataSource(Properties props) throws SQLException {
        try {
            OracleXADataSource ds = new OracleXADataSource();
            setProperties(ds, props);
            return ds;
        }
        catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public Driver createDriver(Properties props) throws SQLException {
        return new OracleDriver();
    }

    private void setProperties(oracle.jdbc.datasource.impl.OracleDataSource ds, Properties properties)
        throws Exception {
        Properties props = (Properties) properties.clone();

        String url = (String) props.remove(DataSourceFactory.JDBC_URL);
        if (url != null) {
            ds.setURL(url);
        }

        String databaseName = (String) props.remove(DataSourceFactory.JDBC_DATABASE_NAME);
        if (databaseName == null && url == null) {
            throw new SQLException("missing required property " + DataSourceFactory.JDBC_DATABASE_NAME);
        }
        ds.setDatabaseName(databaseName);

        String serverName = (String) props.remove(DataSourceFactory.JDBC_SERVER_NAME);
        ds.setServerName(serverName);

        String portNumber = (String) props.remove(DataSourceFactory.JDBC_PORT_NUMBER);
        if (portNumber != null) {
            int portNum = Integer.parseInt(portNumber);
            ds.setPortNumber(portNum);
        }

        String user = (String) props.remove(DataSourceFactory.JDBC_USER);
        ds.setUser(user);

        String password = (String) props.remove(DataSourceFactory.JDBC_PASSWORD);
        ds.setPassword(password);

        if (!props.isEmpty()) {
            BeanConfig.configure(ds, props);
        }
    }

}
