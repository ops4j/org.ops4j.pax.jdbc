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
package org.ops4j.pax.jdbc.derby.impl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.Assert;
import org.junit.Test;
import org.ops4j.pax.jdbc.derby.constants.ConnectionConstant;
import org.osgi.service.jdbc.DataSourceFactory;

public class TestDsf {
    @Test
    public void testPropertyBased() throws SQLException, IOException {
        DerbyDataSourceFactory dsf = new DerbyDataSourceFactory();
        Properties props = new Properties();
        props.put(DataSourceFactory.JDBC_DATABASE_NAME, "target/test1");
        props.put(ConnectionConstant.CREATE_DATABASE, "create");
        DataSource ds = dsf.createDataSource(props);
        EmbeddedDataSource eds = (EmbeddedDataSource)ds;
        Assert.assertEquals("target/test1", eds.getDatabaseName());
        Assert.assertEquals("create", eds.getCreateDatabase());
        Connection con = ds.getConnection();
        con.close();
    }
    
    @Test
    public void testUrlBased() throws SQLException, IOException {
        DerbyDataSourceFactory dsf = new DerbyDataSourceFactory();
        Properties props = new Properties();
        props.put(DataSourceFactory.JDBC_URL, "jdbc:derby:target/test;create=true");
        DataSource ds = dsf.createDataSource(props);
        EmbeddedDataSource eds = (EmbeddedDataSource)ds;
        Assert.assertEquals("target/test", eds.getDatabaseName());
        Assert.assertEquals("create=true", eds.getConnectionAttributes());
        Connection con = ds.getConnection();
        con.close();
    }
}
