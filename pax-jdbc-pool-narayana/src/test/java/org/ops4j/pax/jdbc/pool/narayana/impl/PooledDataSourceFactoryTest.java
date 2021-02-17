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
package org.ops4j.pax.jdbc.pool.narayana.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.ConnectionEventListener;
import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.dbcp2.managed.ManagedDataSource;
import org.jboss.tm.XAResourceRecovery;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.jdbc.DataSourceFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PooledDataSourceFactoryTest {

    @Test
    public void testcreateDataSourceWithXA() throws SQLException {
        DataSourceFactory dsf = mock(DataSourceFactory.class);
        XADataSource xads = mock(XADataSource.class);
        when(dsf.createXADataSource(any(Properties.class))).thenReturn(xads);
        BundleContext context = mock(BundleContext.class);
        @SuppressWarnings("unchecked")
        ServiceRegistration<XAResourceRecovery> registration = (ServiceRegistration<XAResourceRecovery>) mock(ServiceRegistration.class);
        when(context.registerService(eq(XAResourceRecovery.class), any(XAResourceRecovery.class), eq(null))).thenReturn(registration);
        XAConnection xaConnection = mock(XAConnection.class);
        when(xads.getXAConnection()).thenReturn(xaConnection);
        Connection connection = mock(Connection.class);
        when(xaConnection.getConnection()).thenReturn(connection);
        XAResource xaResource = mock(XAResource.class);
        when(xaConnection.getXAResource()).thenReturn(xaResource);
        when(connection.isClosed()).thenReturn(false);
        when(connection.isReadOnly()).thenReturn(false);
        when(connection.getAutoCommit()).thenReturn(false);

        for (int i = 0; i < Integer.parseInt(createValidProps().getProperty("pool.initialSize")); i++) {
            xaConnection.addConnectionEventListener(any(ConnectionEventListener.class));
            connection.rollback();
            connection.clearWarnings();
            connection.setAutoCommit(true);
        }

        TransactionManager tm = mock(TransactionManager.class);
        DbcpXAPooledDataSourceFactory pdsf = new DbcpXAPooledDataSourceFactory(context, tm);
        DataSource ds = pdsf.create(dsf, createValidProps());

        assertTrue(ds instanceof ManagedDataSource);

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
        Connection connection = mock(Connection.class);
        when(dsf.createDataSource(any(Properties.class))).thenReturn(exds);
        when(exds.getConnection()).thenReturn(connection);
        when(connection.isClosed()).thenReturn(false);
        when(connection.getAutoCommit()).thenReturn(true);

        for (int i = 0; i < Integer.parseInt(createValidProps().getProperty("pool.initialSize")); i++) {
            connection.clearWarnings();
        }

        DbcpPooledDataSourceFactory pdsf = new DbcpPooledDataSourceFactory();
        DataSource ds = pdsf.create(dsf, createValidProps());

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
