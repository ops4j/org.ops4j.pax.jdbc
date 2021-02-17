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
package org.ops4j.pax.jdbc.config.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.jdbc.DataSourceFactory;

public class DataSourceRegistrationTest {

    private static final String H2_DRIVER_CLASS = "org.h2.Driver";

    @Test
    @SuppressWarnings("unchecked")
    public void testPublishedAndUnpublished() throws ConfigurationException, InvalidSyntaxException, SQLException {
        ArgumentCaptor<Properties> capturedDsProps = ArgumentCaptor.forClass(Properties.class);
        @SuppressWarnings("rawtypes")
        ArgumentCaptor<Dictionary> capturedServiceProps = ArgumentCaptor.forClass(Dictionary.class);

        BundleContext context = mock(BundleContext.class);
        final DataSourceFactory dsf = mock(DataSourceFactory.class);

        // Expect that a DataSource is created using the DataSourceFactory
        DataSource ds = mock(DataSource.class);
        when(dsf.createDataSource(capturedDsProps.capture())).thenReturn(ds);

        // Expect DataSource is registered as a service
        ServiceRegistration<?> dsSreg = mock(ServiceRegistration.class);
        when(context.registerService(eq(DataSource.class.getName()), eq(ds), capturedServiceProps.capture()))
                .thenReturn(dsSreg);

        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, H2_DRIVER_CLASS);
        properties.put(DataSourceFactory.JDBC_DATABASE_NAME, "mydbname");
        properties.put(DataSourceFactory.JDBC_DATASOURCE_NAME, "myDsName");
        DataSourceRegistration publisher = new DataSourceRegistration(context, dsf, properties, properties, null);

        // Check that correct properties were sent to DataSourceFactory
        Properties dsProps = capturedDsProps.getValue();
        assertEquals("mydbname", dsProps.get(DataSourceFactory.JDBC_DATABASE_NAME));

        // Check that correct properties were set on the DataSource service
        Dictionary<?, ?> serviceProps = capturedServiceProps.getValue();
        assertEquals("myDsName", serviceProps.get(DataSourceFactory.JDBC_DATASOURCE_NAME));
        assertEquals("myDsName", serviceProps.get("osgi.jndi.service.name"));

        reset(context);

        publisher.close();
        verify(dsSreg).unregister();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPublishedConnectionPoolDS() throws ConfigurationException, InvalidSyntaxException, SQLException {
        BundleContext context = mock(BundleContext.class);
        final DataSourceFactory dsf = mock(DataSourceFactory.class);

        // Expect that a ConnectionPoolDataSource is created using the DataSourceFactory
        ConnectionPoolDataSource cpds = mock(ConnectionPoolDataSource.class);
        when(dsf.createConnectionPoolDataSource(any(Properties.class))).thenReturn(cpds);

        // Expect DataSource is registered as a service
        ServiceRegistration<?> dsSreg = mock(ServiceRegistration.class);
        when(context.registerService(eq(ConnectionPoolDataSource.class.getName()), eq(cpds), any(Dictionary.class)))
                .thenReturn(dsSreg);

        // create and publish the datasource
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(DataSourceRegistration.JNDI_SERVICE_NAME, "test");
        properties.put(DataSourceRegistration.DATASOURCE_TYPE, ConnectionPoolDataSource.class.getSimpleName());
        DataSourceRegistration publisher = new DataSourceRegistration(context, dsf, properties, properties, null);
        publisher.close();
        verify(dsSreg).unregister();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPublishedXADS() throws ConfigurationException, InvalidSyntaxException, SQLException {
        BundleContext context = mock(BundleContext.class);
        final DataSourceFactory dsf = mock(DataSourceFactory.class);

        // Expect that a ConnectionPoolDataSource is created using the DataSourceFactory
        XADataSource xads = mock(XADataSource.class);
        when(dsf.createXADataSource(any(Properties.class))).thenReturn(xads);

        // Expect DataSource is registered as a service
        ServiceRegistration<?> dsSreg = mock(ServiceRegistration.class);

        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(DataSourceRegistration.JNDI_SERVICE_NAME, "test");
        properties.put(DataSourceRegistration.DATASOURCE_TYPE, XADataSource.class.getSimpleName());
        new DataSourceRegistration(context, dsf, properties, properties, null);
        verify(context).registerService(eq(XADataSource.class.getName()), eq(xads), any(Dictionary.class));
    }

    @SuppressWarnings("resource")
    @Test(expected = IllegalArgumentException.class)
    public void testError() throws ConfigurationException, InvalidSyntaxException, SQLException {
        BundleContext context = mock(BundleContext.class);
        final DataSourceFactory dsf = mock(DataSourceFactory.class);

        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(DataSourceRegistration.JNDI_SERVICE_NAME, "test");
        properties.put(DataSourceRegistration.DATASOURCE_TYPE, "something else");
        new DataSourceRegistration(context, dsf, properties, properties, null);
        verify(context).registerService(eq(DataSource.class.getName()), any(DataSource.class), any(Dictionary.class));
    }

}
