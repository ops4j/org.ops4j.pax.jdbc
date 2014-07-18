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

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.jdbc.DataSourceFactory;


public class DataSourceManagerTest {

    private static final String TESTPID = "testpid";
    private static final String H2_DRIVER_CLASS = "org.h2.Driver";

    @Test
    public void testUpdatedAndDeleted() throws ConfigurationException, InvalidSyntaxException, SQLException {
        IMocksControl c = EasyMock.createControl();
        BundleContext context = c.createMock(BundleContext.class);
        
        final DataSourceFactory dsf = c.createMock(DataSourceFactory.class);
        
        String expectedFilter = "(&(objectClass=org.osgi.service.jdbc.DataSourceFactory)(osgi.jdbc.driver.class=org.h2.Driver))";
        
        Filter filter = FrameworkUtil.createFilter(expectedFilter);
        expect(context.createFilter(expectedFilter)).andReturn(filter );
        
        expect(context.getProperty("org.osgi.framework.version")).andReturn("1.5.0");
        
        context.addServiceListener(EasyMock.anyObject(ServiceListener.class), EasyMock.eq(expectedFilter));
        expectLastCall();
        
        ServiceReference ref = c.createMock(ServiceReference.class);
        ServiceReference[] refs = new ServiceReference[]{ref };
        expect(context.getServiceReferences((String)null, expectedFilter)).andReturn(refs);
        
        expect(context.getService(ref)).andReturn(dsf);
        
        final DataSourcePublisher publisher = c.createMock(DataSourcePublisher.class);
        publisher.publish(dsf);
        expectLastCall();
        
        expect(context.ungetService(ref)).andReturn(true);

        DataSourceManager dsManager = new DataSourceManager(context) {

            @SuppressWarnings("rawtypes")
            @Override
            protected DataSourcePublisher createPublisher(Dictionary config) {
                return publisher;
            }
            
        };

        c.replay();
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, H2_DRIVER_CLASS);
        properties.put(DataSourceFactory.JDBC_DATABASE_NAME, "mydbname");
        dsManager.updated(TESTPID, properties);
        c.verify();
        
        c.reset();
        
        context.removeServiceListener(EasyMock.anyObject(ServiceListener.class));
        publisher.unpublish();
        expectLastCall();
        c.replay();
        dsManager.updated(TESTPID, null);
        c.verify();
    }
    
    @Test
    public void testNotEnoughInfoToFindDriver() throws ConfigurationException, InvalidSyntaxException, SQLException {
        IMocksControl c = EasyMock.createControl();
        BundleContext context = c.createMock(BundleContext.class);
        DataSourceManager dsManager = new DataSourceManager(context);

        c.replay();
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put("other", "value");
        try {
            dsManager.updated(TESTPID, properties);
        } catch (ConfigurationException e) {
            Assert.assertEquals("Could not determine driver to use. Specify either osgi.jdbc.driver.class or osgi.jdbc.driver.name", e.getReason());
        }
        c.verify();
    }

}
