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
package org.ops4j.pax.jdbc.saphana.impl;

import com.sap.db.jdbcext.AbstractDataSource;
import com.sap.db.jdbcext.HanaConnectionPoolDataSource;
import com.sap.db.jdbcext.HanaDataSource;
import com.sap.db.jdbcext.HanaXADataSource;
import org.ops4j.pax.jdbc.common.BeanConfig;
import org.osgi.service.jdbc.DataSourceFactory;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

public class SapHanaDataSourceFactory implements DataSourceFactory {

    @Override
    public DataSource createDataSource(Properties props) throws SQLException {
        HanaDataSource ds = new HanaDataSource();
        setProperties(ds, props);
        return ds;
    }

    private void setProperties(AbstractDataSource ds, Properties properties) throws SQLException {
        Properties props = (Properties) properties.clone();
        String url = (String) props.remove(DataSourceFactory.JDBC_URL);

        if (url == null) {
            throw new SQLException("missing required property "
                    + DataSourceFactory.JDBC_URL);
        }
        ds.setUrl(url);

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
        HanaConnectionPoolDataSource ds = new HanaConnectionPoolDataSource();
        setProperties(ds, props);
        return ds;
    }

    @Override
    public XADataSource createXADataSource(Properties props) throws SQLException {
        HanaXADataSource ds = new HanaXADataSource();
        setProperties(ds, props);
        return ds;
    }

    @Override
    public Driver createDriver(Properties props) {
        com.sap.db.jdbc.Driver driver = new com.sap.db.jdbc.Driver();
        return driver;
    }
}
