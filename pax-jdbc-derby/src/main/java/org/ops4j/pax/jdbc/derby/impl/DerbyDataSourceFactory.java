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
package org.ops4j.pax.jdbc.derby.impl;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.apache.derby.jdbc.EmbeddedDriver;
import org.apache.derby.jdbc.EmbeddedXADataSource;
import org.apache.derby.jdbc.ReferenceableDataSource;
import org.ops4j.pax.jdbc.derby.constants.ConnectionConstant;
import org.osgi.service.jdbc.DataSourceFactory;

public class DerbyDataSourceFactory implements DataSourceFactory {

    private static final String DERBY_PREFIX = "jdbc:derby:";

    @Override
    public DataSource createDataSource(Properties props) throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        setProperties(ds, props);
        return ds;
    }

    private void setProperties(ReferenceableDataSource ds, Properties properties)
        throws SQLException {
        Properties props = (Properties) properties.clone();
        String databaseName = (String) props.remove(DataSourceFactory.JDBC_DATABASE_NAME);
        if (databaseName != null) {
            ds.setDatabaseName(databaseName);
        }
        
        String createDatabase = (String) props.remove(ConnectionConstant.CREATE_DATABASE);
        ds.setCreateDatabase(createDatabase);

        String password = (String) props.remove(DataSourceFactory.JDBC_PASSWORD);
        ds.setPassword(password);

        String user = (String) props.remove(DataSourceFactory.JDBC_USER);
        ds.setUser(user);
        
        String url = (String) props.remove(DataSourceFactory.JDBC_URL);
        applyUrl(ds, url);

        if (!props.isEmpty()) {
            throw new SQLException("cannot set properties " + props.keySet());
        }
    }

    private void applyUrl(ReferenceableDataSource ds, String url) {
        if (url == null) {
            return;
        }
        if (!url.startsWith(DERBY_PREFIX)) {
            throw new IllegalArgumentException("The supplied URL is no derby url: " + url);
        }
        String suburl = url.substring(DERBY_PREFIX.length());
        String[] parts = suburl.split(";");
        String database = parts[0];
        if (database != null) {
            ds.setDatabaseName(database);
        }
        if (parts.length > 1) {
            String options = parts[1];
            if (options.length() > 0) {
                ds.setConnectionAttributes(options);
            }
        }
    }

    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource(Properties props)
        throws SQLException {
        EmbeddedConnectionPoolDataSource ds = new EmbeddedConnectionPoolDataSource();
        setProperties(ds, props);
        return ds;
    }

    @Override
    public XADataSource createXADataSource(Properties props) throws SQLException {
        EmbeddedXADataSource ds = new EmbeddedXADataSource();
        setProperties(ds, props);
        return ds;
    }

    @Override
    public Driver createDriver(Properties props) throws SQLException {
        EmbeddedDriver driver = new EmbeddedDriver();
        return driver;
    }

}
