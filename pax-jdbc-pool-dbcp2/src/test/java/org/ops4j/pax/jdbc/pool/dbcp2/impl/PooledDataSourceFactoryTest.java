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
package org.ops4j.pax.jdbc.pool.dbcp2.impl;

import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;

import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.dbcp2.managed.ManagedDataSource;
import org.junit.Test;
import org.osgi.service.jdbc.DataSourceFactory;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PooledDataSourceFactoryTest {

    @Test
    public void testcreateDataSourceWithXA() throws SQLException {
        DataSourceFactory dsf = mock(DataSourceFactory.class);
        XADataSource xads = mock(XADataSource.class);
        when(dsf.createXADataSource(any(Properties.class))).thenReturn(xads);
        TransactionManager tm = mock(TransactionManager.class);

        DbcpXAPooledDataSourceFactory pdsf = new DbcpXAPooledDataSourceFactory(tm);
        DataSource ds = pdsf.create(dsf, createValidProps());
        assertEquals(ManagedDataSource.class, ds.getClass());

        try {
            pdsf.create(dsf, createInvalidPoolConfig());
        } catch (IllegalArgumentException e) {
            assertEquals("Error setting property dummy:No setter in class org.apache.commons.pool2.impl.GenericObjectPoolConfig for property dummy", e.getMessage());
        }

        try {
            pdsf.create(dsf, createInvalidFactoryConfig());
        } catch (IllegalArgumentException e) {
            assertEquals("Error setting property dummy:No setter in class org.apache.commons.dbcp2.managed.PoolableManagedConnectionFactory for property dummy", e.getMessage());
        }
    }

    @Test
    public void testcreateDataSource() throws SQLException {
        DataSourceFactory dsf = mock(DataSourceFactory.class);
        DataSource exds = mock(DataSource.class);
        DbcpPooledDataSourceFactory pdsf = new DbcpPooledDataSourceFactory();

        DataSource ds = pdsf.create(dsf, createValidProps());
        verify(dsf).createDataSource(any(Properties.class));
        assertEquals(PoolingDataSource.class, ds.getClass());

        try {
            pdsf.create(dsf, createInvalidPoolConfig());
        } catch (IllegalArgumentException e) {
            assertEquals("Error setting property dummy:No setter in class org.apache.commons.pool2.impl.GenericObjectPoolConfig for property dummy", e.getMessage());
        }

        try {
            pdsf.create(dsf, createInvalidFactoryConfig());
        } catch (IllegalArgumentException e) {
            assertEquals("Error setting property dummy:No setter in class org.apache.commons.dbcp2.PoolableConnectionFactory for property dummy", e.getMessage());
        }
    }

    private Properties createValidProps() {
        Properties props = new Properties();
        props.put("pool.maxTotal", "8");
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
