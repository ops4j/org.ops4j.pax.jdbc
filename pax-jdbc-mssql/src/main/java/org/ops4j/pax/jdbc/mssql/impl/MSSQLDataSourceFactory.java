/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ops4j.pax.jdbc.mssql.impl;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.ops4j.pax.jdbc.common.BeanConfig;
import org.osgi.service.jdbc.DataSourceFactory;

import com.microsoft.sqlserver.jdbc.SQLServerConnectionPoolDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerDriver;
import com.microsoft.sqlserver.jdbc.SQLServerXADataSource;

public class MSSQLDataSourceFactory implements DataSourceFactory {

    @Override
    public DataSource createDataSource(Properties props) throws SQLException {
        SQLServerDataSource ds = new SQLServerDataSource();
        return setProperties(ds, props);
    }

    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource(Properties props) throws SQLException {
        SQLServerConnectionPoolDataSource ds = new SQLServerConnectionPoolDataSource();
        return setProperties(ds, props);
    }

    @Override
    public XADataSource createXADataSource(Properties props) throws SQLException {
        SQLServerXADataSource ds = new SQLServerXADataSource();
        return setProperties(ds, props);
    }

    @Override
    public Driver createDriver(Properties props) throws SQLException {
        return new SQLServerDriver();
    }

    @SuppressWarnings("unchecked")
    private <T> T setProperties(SQLServerDataSource ds, Properties props) {
        String url = (String) props.remove(DataSourceFactory.JDBC_URL);
        ds.setURL(url);

        String databaseName = (String) props.remove(DataSourceFactory.JDBC_DATABASE_NAME);
        ds.setDatabaseName(databaseName);

        String serverName = (String) props.remove(DataSourceFactory.JDBC_SERVER_NAME);
        ds.setServerName(serverName);

        String portNumber = (String) props.remove(DataSourceFactory.JDBC_PORT_NUMBER);
        if (portNumber != null) {
            ds.setPortNumber(Integer.parseInt(portNumber));
        }

        String user = (String) props.remove(DataSourceFactory.JDBC_USER);
        ds.setUser(user);

        String password = (String) props.remove(DataSourceFactory.JDBC_PASSWORD);
        ds.setPassword(password);

        if (!props.isEmpty()) {
            BeanConfig.configure(ds, props);
        }

        return (T) ds;
    }
}
