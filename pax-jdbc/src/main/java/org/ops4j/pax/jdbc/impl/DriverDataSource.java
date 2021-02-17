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
package org.ops4j.pax.jdbc.impl;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;

import javax.sql.DataSource;

import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DriverDataSource implements DataSource {

    private static final Logger LOG = LoggerFactory.getLogger(DriverDataSource.class);

    private final Driver driver;
    private final String url;
    private final String user;
    private final String password;

    public DriverDataSource(Driver driver, String url, String user, String password) {
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        LOG.warn("setLogWriter() has no effect");
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        LOG.warn("setLoginTimeout() has no effect");
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isAssignableFrom(DriverDataSource.class)) {
            return iface.cast(this);
        }
        throw new SQLException(DriverDataSource.class.getName() + " cannot be unwrapped to "
            + iface.getName());
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isAssignableFrom(DriverDataSource.class);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getConnection(user, password);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Properties props = new Properties();
        if (username != null) {
            props.setProperty(DataSourceFactory.JDBC_USER, username);
        }
        if (password != null) {
            props.setProperty(DataSourceFactory.JDBC_PASSWORD, password);
        }
        return driver.connect(url, props);
    }

    /**
     * Method added in JDBC 4.1/JDK 7. By not adding the {@code @Override} annotation we stay
     * compatible with JDK 6.
     * 
     * @return
     * @throws SQLFeatureNotSupportedException
     */
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("this datasource does not use java.util.logging");
    }
}
