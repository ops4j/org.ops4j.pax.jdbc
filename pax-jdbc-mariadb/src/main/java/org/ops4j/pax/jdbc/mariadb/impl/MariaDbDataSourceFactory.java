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
package org.ops4j.pax.jdbc.mariadb.impl;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.mariadb.jdbc.MariaDbDataSource;
import org.ops4j.pax.jdbc.common.BeanConfig;
import org.osgi.service.jdbc.DataSourceFactory;

public class MariaDbDataSourceFactory implements DataSourceFactory {

    @Override
    public DataSource createDataSource(Properties props) throws SQLException {
        MariaDbDataSource ds = new MariaDbDataSource();
        setProperties(ds, props);
        return ds;
    }

    private void setProperties(MariaDbDataSource ds, Properties properties) throws SQLException {
        Properties props = (Properties) properties.clone();
        String url = (String) props.remove(DataSourceFactory.JDBC_URL);
        if (url != null) {
            ds.setUrl(url);
        }

        // since driver version 3, databaseName, portNumber and serverName (hostname) are no longer
        // passed individually

        String user = (String) props.remove(DataSourceFactory.JDBC_USER);
        ds.setUser(user);

        String password = (String) props.remove(DataSourceFactory.JDBC_PASSWORD);
        ds.setPassword(password);

        if (!props.isEmpty()) {
            BeanConfig.configure(ds, props);
        }
    }

    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource(Properties props)
        throws SQLException {
        MariaDbDataSource ds = new MariaDbDataSource();
        setProperties(ds, props);
        return ds;
    }

    @Override
    public XADataSource createXADataSource(Properties props) throws SQLException {
        MariaDbDataSource ds = new MariaDbDataSource();
        setProperties(ds, props);
        return ds;
    }

    @Override
    public Driver createDriver(Properties props) throws SQLException {
        org.mariadb.jdbc.Driver driver = new org.mariadb.jdbc.Driver();
        return driver;
    }
}
