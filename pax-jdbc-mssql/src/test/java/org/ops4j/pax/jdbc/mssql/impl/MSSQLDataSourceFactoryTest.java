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

import java.sql.SQLException;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import com.microsoft.sqlserver.jdbc.SQLServerConnectionPoolDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerDriver;
import com.microsoft.sqlserver.jdbc.SQLServerXADataSource;

public class MSSQLDataSourceFactoryTest {
    private static final String URL = "testurl";
    private static final String DB = "testdb";
    private static final String SERVER = "testhost";
    private static final String PORT = "1433";
    private static final String USER = "testuser";
    private static final String PASSWORD = "testpassword";

    @Test
    public void testDS() throws SQLException, ClassNotFoundException {
        MSSQLDataSourceFactory dsf = new MSSQLDataSourceFactory();
        Properties props = testProps();
        SQLServerDataSource ds = (SQLServerDataSource)dsf.createDataSource(props);
        validateDS(ds);
    }
    
    @Test
    public void testConnectionPoolDS() throws SQLException, ClassNotFoundException {
        MSSQLDataSourceFactory dsf = new MSSQLDataSourceFactory();
        Properties props = testProps();
        SQLServerConnectionPoolDataSource ds = (SQLServerConnectionPoolDataSource)dsf.createConnectionPoolDataSource(props);
        validateDS(ds);
    }
    
    @Test
    public void testXADS() throws SQLException, ClassNotFoundException {
        MSSQLDataSourceFactory dsf = new MSSQLDataSourceFactory();
        Properties props = testProps();
        SQLServerXADataSource ds = (SQLServerXADataSource)dsf.createXADataSource(props);
        validateDS(ds);
    }
    
    @Test
    public void testDriver() throws SQLException, ClassNotFoundException {
        MSSQLDataSourceFactory dsf = new MSSQLDataSourceFactory();
        Properties props = testProps();
        SQLServerDriver driver = (SQLServerDriver)dsf.createDriver(props);
        Assert.assertNotNull(driver);
    }
    
    @Test
    public void testEmptyProps() throws SQLException, ClassNotFoundException {
        MSSQLDataSourceFactory dsf = new MSSQLDataSourceFactory();
        Properties props = new Properties();
        SQLServerDataSource ds = (SQLServerDataSource)dsf.createDataSource(props);
        Assert.assertNotNull(ds);
    }

    private void validateDS(SQLServerDataSource ds) {
        Assert.assertEquals(URL, ds.url);
        Assert.assertEquals(DB, ds.dbName);
        Assert.assertEquals(SERVER, ds.serverName);
        Assert.assertEquals(1433, ds.portNumber);
        Assert.assertEquals(USER, ds.user);
        Assert.assertEquals(PASSWORD, ds.password);
    }

    private Properties testProps() {
        Properties props = new Properties();
        props.put("url", URL);
        props.put("databaseName", DB);
        props.put("serverName", SERVER);
        props.put("portNumber", PORT);
        props.put("user", USER);
        props.put("password", PASSWORD);
        return props;
    }

}
