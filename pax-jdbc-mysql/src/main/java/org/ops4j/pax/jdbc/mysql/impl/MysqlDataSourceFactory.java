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
package org.ops4j.pax.jdbc.mysql.impl;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.ops4j.pax.jdbc.common.BeanConfig;
import org.osgi.service.jdbc.DataSourceFactory;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlXADataSource;

public class MysqlDataSourceFactory implements DataSourceFactory {

    @Override
    public DataSource createDataSource(Properties props) throws SQLException {
        MysqlDataSource ds = new MysqlDataSource();
        setProperties(ds, props);
        return ds;
    }

    private void setProperties(MysqlDataSource ds, Properties properties) throws SQLException {
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
            BeanConfig.configure(ds, props);
        }
    }

    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource(Properties props)
        throws SQLException {
        MysqlConnectionPoolDataSource ds = new MysqlConnectionPoolDataSource();
        setProperties(ds, props);
        return ds;
    }

    @Override
    public XADataSource createXADataSource(Properties props) throws SQLException {
        MysqlXADataSource ds = new MysqlXADataSource();
        setProperties(ds, props);
        return ds;
    }

    @Override
    public Driver createDriver(Properties props) throws SQLException {
        com.mysql.jdbc.Driver driver = new com.mysql.jdbc.Driver();
        return driver;
    }
}
