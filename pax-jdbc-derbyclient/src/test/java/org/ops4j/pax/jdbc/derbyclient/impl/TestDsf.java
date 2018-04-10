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

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.derby.jdbc.ClientDataSource;
import org.junit.Assert;
import org.junit.Test;
import org.ops4j.pax.jdbc.derbyclient.constants.ClientConnectionConstant;
import org.osgi.service.jdbc.DataSourceFactory;

public class TestDsf {

    @Test
    public void testPropertyBased() throws SQLException, IOException {
        DerbyClientDatasourceFactory dsf = new DerbyClientDatasourceFactory();
        Properties props = new Properties();
        props.put(DataSourceFactory.JDBC_DATABASE_NAME, "target/test1");
        props.put(ClientConnectionConstant.CREATE_DATABASE, "create");
        props.put(DataSourceFactory.JDBC_USER, "derby");
        DataSource ds = dsf.createDataSource(props);
        ClientDataSource eds = (ClientDataSource)ds;
        Assert.assertEquals("target/test1", eds.getDatabaseName());
        Assert.assertEquals("create", eds.getCreateDatabase());
    }
    
    @Test
    public void testUrlBased() throws SQLException, IOException {
        DerbyClientDatasourceFactory dsf = new DerbyClientDatasourceFactory();
        Properties props = new Properties();
        props.put(DataSourceFactory.JDBC_URL, "jdbc:derby://localhost:15527/target/test;create=true");
        props.put(DataSourceFactory.JDBC_USER, "derby");
        DataSource ds = dsf.createDataSource(props);
        ClientDataSource eds = (ClientDataSource)ds;
        Assert.assertEquals("target/test", eds.getDatabaseName());
        Assert.assertEquals("localhost", eds.getServerName());
        Assert.assertEquals(15527, eds.getPortNumber());
        Assert.assertEquals("create=true", eds.getConnectionAttributes());
    }
    
}
