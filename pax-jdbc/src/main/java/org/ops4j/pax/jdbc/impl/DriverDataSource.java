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
package org.ops4j.pax.jdbc.impl;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.osgi.service.jdbc.DataSourceFactory;

public class DriverDataSource implements DataSource
{
    private Driver driver;
    private String url;
    private String user;
    private String password;
    
    public DriverDataSource(Driver driver, String url, String user, String password)
    {
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
    }
    
    @Override
    public PrintWriter getLogWriter() throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setLogWriter( PrintWriter out ) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setLoginTimeout( int seconds ) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int getLoginTimeout() throws SQLException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public <T> T unwrap( Class<T> iface ) throws SQLException
    {
        throw new SQLException("not a wrapper");
    }

    @Override
    public boolean isWrapperFor( Class<?> iface ) throws SQLException
    {
        return false;
    }

    @Override
    public Connection getConnection() throws SQLException
    {
        return getConnection(user, password);
    }

    @Override
    public Connection getConnection( String username, String password ) throws SQLException
    {
        Properties props = new Properties();
        if (user != null)
        {
            props.setProperty( DataSourceFactory.JDBC_USER, user );
        }
        if (password != null)
        {
            props.setProperty( DataSourceFactory.JDBC_PASSWORD, password );
        }
        return driver.connect( url, props );
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException
    {
        throw new SQLFeatureNotSupportedException("this datasource does not use java.util.logging");
    }
}
