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
package org.ops4j.pax.jdbc.derbyclient.impl;

import java.net.URI;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.apache.derby.jdbc.ClientConnectionPoolDataSource;
import org.apache.derby.jdbc.ClientDataSource;
import org.apache.derby.jdbc.ClientDriver;
import org.apache.derby.jdbc.ClientXADataSource;
import org.ops4j.pax.jdbc.derbyclient.constants.ClientConnectionConstant;
import org.osgi.service.jdbc.DataSourceFactory;

public class DerbyClientDatasourceFactory implements DataSourceFactory {
    private static final String DERBY_PREFIX = "jdbc:derby:";
    // private Logger LOG = LoggerFactory.getLogger(DerbyClientDatasourceFactory.class);

    @Override
    public DataSource createDataSource(Properties props) throws SQLException {
        ClientDataSource ds = new ClientDataSource();
        setProperties(ds, props);
        return ds;
    }

    private void setProperties(ClientDataSource ds, Properties properties) throws SQLException {
        Properties props = (Properties) properties.clone();
        String databaseName = (String) props.remove(DataSourceFactory.JDBC_DATABASE_NAME);
        if (databaseName != null) {
            ds.setDatabaseName(databaseName);
        }

        String password = (String) props.remove(DataSourceFactory.JDBC_PASSWORD);
        ds.setPassword(password);

        String user = (String) props.remove(DataSourceFactory.JDBC_USER);
        ds.setUser(user);

        String createDatabase = (String) props.remove(ClientConnectionConstant.CREATE_DATABASE);
        ds.setCreateDatabase(createDatabase);
        String host = (String) properties.remove(DataSourceFactory.JDBC_SERVER_NAME);
        if (host == null) {
            host = "localhost";
        }

        ds.setServerName(host);
        String portNumber = (String) props.remove(DataSourceFactory.JDBC_PORT_NUMBER);
        if (portNumber != null) {
            ds.setPortNumber(Integer.parseInt(portNumber));
        }
        else {
            ds.setPortNumber(1527);
        }
        
        String url = (String) props.remove(DataSourceFactory.JDBC_URL);
        applyUrl(ds, url);
    }
    
    private void applyUrl(ClientDataSource ds, String url) {
        if (url == null) {
            return;
        }
        if (!url.startsWith(DERBY_PREFIX)) {
            throw new IllegalArgumentException("The supplied URL is no derby url: " + url);
        }
        URI uri = URI.create(url.substring(5));
        ds.setServerName(uri.getHost());
        ds.setPortNumber(uri.getPort());
        String suburl = uri.getPath();
        if (suburl.startsWith("/")) {
            suburl = suburl.substring(1);
        }
        String[] parts = suburl.split(";");
        String database = parts[0];
        if (database != null) {
            ds.setDatabaseName(database);
        }
        String options = parts[1];
        if (options.length() > 0) {
            ds.setConnectionAttributes(options);
        }
    }

    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource(Properties props)
        throws SQLException {
        ClientConnectionPoolDataSource ds = new ClientConnectionPoolDataSource();
        setProperties(ds, props);
        return ds;
    }

    @Override
    public XADataSource createXADataSource(Properties props) throws SQLException {
        ClientXADataSource ds = new ClientXADataSource();
        setProperties(ds, props);
        return ds;
    }

    @Override
    public Driver createDriver(Properties props) throws SQLException {
        ClientDriver driver = new ClientDriver();
        return driver;
    }

}
