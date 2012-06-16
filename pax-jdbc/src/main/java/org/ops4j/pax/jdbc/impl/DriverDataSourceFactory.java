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

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.osgi.service.jdbc.DataSourceFactory;

public class DriverDataSourceFactory implements DataSourceFactory
{
    private Driver driver;
    
    public DriverDataSourceFactory(Driver driver)
    {
        this.driver = driver;
    }
    
    public DataSource createDataSource( Properties props ) throws SQLException
    {
        String url = props.getProperty( JDBC_URL );
        String user = props.getProperty( JDBC_USER );
        String password = props.getProperty( JDBC_PASSWORD );
        return new DriverDataSource( driver, url, user, password );
    }

    public ConnectionPoolDataSource createConnectionPoolDataSource( Properties props )
        throws SQLException
    {
        throw new SQLException( "not supported - use a driver adapter org.ops4j.pax.jdbc.<subprotocol>" );
    }

    public XADataSource createXADataSource( Properties props ) throws SQLException
    {
        throw new SQLException( "not supported - use a driver adapter org.ops4j.pax.jdbc.<subprotocol>" );
    }

    public Driver createDriver( Properties props ) throws SQLException
    {
        return driver;
    }
}
