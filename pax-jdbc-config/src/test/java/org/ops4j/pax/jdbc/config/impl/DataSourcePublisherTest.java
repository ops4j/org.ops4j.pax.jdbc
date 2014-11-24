/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.jdbc.config.impl;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.jdbc.DataSourceFactory;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class DataSourcePublisherTest {

    private static final String H2_DRIVER_CLASS = "org.h2.Driver";

    @Test
    public void testPublishedAndUnpublished() throws ConfigurationException,
        InvalidSyntaxException, SQLException {
        Capture<Properties> capturedDsProps = new Capture<Properties>();
        Capture<Dictionary> capturedServiceProps = new Capture<Dictionary>();

        IMocksControl c = EasyMock.createControl();
        BundleContext context = c.createMock(BundleContext.class);
        final DataSourceFactory dsf = c.createMock(DataSourceFactory.class);

        // Expect that a DataSource is created using the DataSourceFactory
        DataSource ds = c.createMock(DataSource.class);
        expect(dsf.createDataSource(capture(capturedDsProps))).andReturn(ds);

        // Expect DataSource is registered as a service
        ServiceRegistration dsSreg = c.createMock(ServiceRegistration.class);

        expect(
            context.registerService(eq(DataSource.class.getName()), eq(ds),
                capture(capturedServiceProps))).andReturn(dsSreg);

        // create and publish the datasource
        c.replay();
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, H2_DRIVER_CLASS);
        properties.put(DataSourceFactory.JDBC_DATABASE_NAME, "mydbname");
        properties.put(DataSourceFactory.JDBC_DATASOURCE_NAME, "myDsName");
        DataSourcePublisher publisher = new DataSourcePublisher(context, properties);
        publisher.publish(dsf);
        c.verify();

        // Check that correct properties were sent to DataSourceFactory
        Properties dsProps = capturedDsProps.getValue();
        assertEquals("mydbname", dsProps.get(DataSourceFactory.JDBC_DATABASE_NAME));

        // Check that correct properties were set on the DataSource service
        Dictionary serviceProps = capturedServiceProps.getValue();
        assertEquals("myDsName", serviceProps.get(DataSourceFactory.JDBC_DATASOURCE_NAME));
        assertEquals("myDsName", serviceProps.get("osgi.jndi.service.name"));

        c.reset();

        // Check unpublish unregisters the services
        dsSreg.unregister();
        expectLastCall();

        c.replay();
        publisher.unpublish();
        c.verify();
    }
    
    @Test
    public void testPublishedConnectionPoolDS() throws ConfigurationException,
        InvalidSyntaxException, SQLException {

        IMocksControl c = EasyMock.createControl();
        BundleContext context = c.createMock(BundleContext.class);
        final DataSourceFactory dsf = c.createMock(DataSourceFactory.class);

        // Expect that a ConnectionPoolDataSource is created using the DataSourceFactory
        ConnectionPoolDataSource cpds = c.createMock(ConnectionPoolDataSource.class);
        expect(dsf.createConnectionPoolDataSource(anyObject(Properties.class))).andReturn(cpds);

        // Expect DataSource is registered as a service
        ServiceRegistration dsSreg = c.createMock(ServiceRegistration.class);
        expect(
            context.registerService(eq(ConnectionPoolDataSource.class.getName()), eq(cpds),
                anyObject(Dictionary.class))).andReturn(dsSreg);

        // create and publish the datasource
        c.replay();
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(DataSourcePublisher.DATASOURCE_TYPE, ConnectionPoolDataSource.class.getSimpleName());
        DataSourcePublisher publisher = new DataSourcePublisher(context, properties);
        publisher.publish(dsf);
        c.verify();
    }
    
    @Test
    public void testPublishedXADS() throws ConfigurationException,
        InvalidSyntaxException, SQLException {

        IMocksControl c = EasyMock.createControl();
        BundleContext context = c.createMock(BundleContext.class);
        final DataSourceFactory dsf = c.createMock(DataSourceFactory.class);

        // Expect that a ConnectionPoolDataSource is created using the DataSourceFactory
        XADataSource xads = c.createMock(XADataSource.class);
        expect(dsf.createXADataSource(anyObject(Properties.class))).andReturn(xads);

        // Expect DataSource is registered as a service
        ServiceRegistration dsSreg = c.createMock(ServiceRegistration.class);
        expect(
            context.registerService(eq(XADataSource.class.getName()), eq(xads),
                anyObject(Dictionary.class))).andReturn(dsSreg);

        // create and publish the datasource
        c.replay();
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(DataSourcePublisher.DATASOURCE_TYPE, XADataSource.class.getSimpleName());
        DataSourcePublisher publisher = new DataSourcePublisher(context, properties);
        publisher.publish(dsf);
        c.verify();
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testError() throws ConfigurationException,
        InvalidSyntaxException, SQLException {

        IMocksControl c = EasyMock.createControl();
        BundleContext context = c.createMock(BundleContext.class);
        final DataSourceFactory dsf = c.createMock(DataSourceFactory.class);

        // create and publish the datasource
        c.replay();
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(DataSourcePublisher.DATASOURCE_TYPE, "something else");
        DataSourcePublisher publisher = new DataSourcePublisher(context, properties);
        publisher.publish(dsf);
        c.verify();
    }

}
