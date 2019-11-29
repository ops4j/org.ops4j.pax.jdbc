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
package org.ops4j.pax.jdbc.pool.narayana.impl;

import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.dbcp2.managed.ManagedDataSource;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.service.jdbc.DataSourceFactory;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class PooledDataSourceFactoryTest {

    @Test
    @Ignore
    public void testcreateDataSourceWithXA() throws SQLException {
        IMocksControl c = EasyMock.createControl();
        DataSourceFactory dsf = c.createMock(DataSourceFactory.class);
        XADataSource xads = c.createMock(XADataSource.class);
        EasyMock.expect(dsf.createXADataSource(EasyMock.anyObject(Properties.class))).andReturn(xads).atLeastOnce();
        BundleContext context = c.createMock(BundleContext.class);
        TransactionManager tm = c.createMock(TransactionManager.class);
        DbcpXAPooledDataSourceFactory pdsf = new DbcpXAPooledDataSourceFactory(context, tm);
        c.replay();
        DataSource ds = pdsf.create(dsf, createValidProps());
        c.verify();
        Assert.assertEquals(ManagedDataSource.class, ds.getClass());

        try {
            pdsf.create(dsf, createInvalidPoolConfig());
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Error setting property dummy:No setter in class org.apache.commons.pool2.impl.GenericObjectPoolConfig for property dummy", e.getMessage());
        }

        try {
            pdsf.create(dsf, createInvalidFactoryConfig());
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Error setting property dummy:No setter in class org.apache.commons.dbcp2.managed.PoolableManagedConnectionFactory for property dummy", e.getMessage());
        }
    }

    @Test
    public void testcreateDataSource() throws SQLException {
        IMocksControl c = EasyMock.createControl();
        DataSourceFactory dsf = c.createMock(DataSourceFactory.class);
        DataSource exds = c.createMock(DataSource.class);
        Connection connection = c.createMock(Connection.class);
        EasyMock.expect(dsf.createDataSource(EasyMock.anyObject(Properties.class))).andReturn(exds).atLeastOnce();
        EasyMock.expect(exds.getConnection()).andReturn(connection).anyTimes();
        EasyMock.expect(connection.isClosed()).andReturn(false).anyTimes();
        EasyMock.expect(connection.getAutoCommit()).andReturn(true).anyTimes();
        for (int i = 0; i < Integer.valueOf(createValidProps().getProperty("pool.initialSize")); i++) {
            connection.clearWarnings();
        }
        DbcpPooledDataSourceFactory pdsf = new DbcpPooledDataSourceFactory();

        c.replay();
        DataSource ds = pdsf.create(dsf, createValidProps());
        c.verify();

        Assert.assertEquals(PoolingDataSource.class, ds.getClass());

        try {
            pdsf.create(dsf, createInvalidPoolConfig());
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Error setting property dummy:No setter in class org.apache.commons.pool2.impl.GenericObjectPoolConfig for property dummy", e.getMessage());
        }

        try {
            pdsf.create(dsf, createInvalidFactoryConfig());
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Error setting property dummy:No setter in class org.apache.commons.dbcp2.PoolableConnectionFactory for property dummy", e.getMessage());
        }
    }
    
    private Properties createValidProps() {
        Properties props = new Properties();
        props.put("pool.maxTotal", "8");
        props.put("pool.initialSize", "4");
        props.put("factory.validationQuery", "dummyQuery");
        return props;
    }

    private Properties createInvalidPoolConfig() {
        Properties props = new Properties();
        props.put("pool.dummy", "8");
        return props;
    }

    private Properties createInvalidFactoryConfig() {
        Properties props2 = new Properties();
        props2.put("factory.dummy", "8");
        return props2;
    }

}
