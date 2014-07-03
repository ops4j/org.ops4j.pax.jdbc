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
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;

import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import javax.sql.DataSource;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
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
        
        // Expect that a DataSource is created using the DataSourceFactory
        DataSource ds = c.createMock(DataSource.class);
        expect(dsf.createDataSource(EasyMock.anyObject(Properties.class))).andReturn(ds);

        // Expect DataSource is registered as a service
        ServiceRegistration sreg = c.createMock(ServiceRegistration.class);
        expect(context.registerService(eq(DataSource.class.getName()), eq(ds), anyObject(Dictionary.class))).andReturn(sreg);

        DataSourceManager dsManager = new DataSourceManager(context) {
            protected DataSourceFactory findDSFactoryForDriverClass(String driverClass) {
                Assert.assertEquals(H2_DRIVER_CLASS, driverClass);
                return dsf;
            }
            
        };

        c.replay();
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, H2_DRIVER_CLASS);
        properties.put(DataSourceFactory.JDBC_DATABASE_NAME, "mydbname");
        dsManager.updated(TESTPID, properties);
        c.verify();
        
        c.reset();
        
        sreg.unregister();
        EasyMock.expectLastCall();
        c.replay();
        dsManager.deleted(TESTPID);
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
    
    /**
     * Check behavior if a DataSourceFactory with the requested driver class is present
     * 
     * @throws InvalidSyntaxException
     * @throws ConfigurationException 
     */
    @Test
    public void testFindDSFactoryForDriverClassFound() throws InvalidSyntaxException, ConfigurationException {
        IMocksControl c = EasyMock.createControl();
        BundleContext context = c.createMock(BundleContext.class);
        ServiceReference ref = c.createMock(ServiceReference.class);
        ServiceReference[] refs = new ServiceReference[]{ref};
        expect(context.getServiceReferences(DataSourceFactory.class.getName(), 
                                            "(osgi.jdbc.driver.class=" + H2_DRIVER_CLASS + ")"))
            .andReturn(refs);
        DataSourceFactory expectedDsf = c.createMock(DataSourceFactory.class);
        expect(context.getService(ref)).andReturn(expectedDsf );
        DataSourceManager dsManager = new DataSourceManager(context);
        c.replay();
        DataSourceFactory dsf = dsManager.findDSFactoryForDriverClass(H2_DRIVER_CLASS);
        Assert.assertSame(expectedDsf, dsf);
        c.verify();
    }
    
    /**
     * Check behavior if a DataSourceFactory with the requested driver class is not present
     * 
     * @throws InvalidSyntaxException
     */
    @Test
    public void testFindDSFactoryForDriverClassNotFound() throws InvalidSyntaxException {
        IMocksControl c = EasyMock.createControl();
        BundleContext context = c.createMock(BundleContext.class);
        ServiceReference[] refs = new ServiceReference[] {};
        expect(context.getServiceReferences(DataSourceFactory.class.getName(), "(osgi.jdbc.driver.class=" + H2_DRIVER_CLASS + ")"))
            .andReturn(refs);
        DataSourceManager dsManager = new DataSourceManager(context);
        c.replay();
        try {
            dsManager.findDSFactoryForDriverClass(H2_DRIVER_CLASS);
        } catch (ConfigurationException e) {
            Assert.assertEquals(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, e.getProperty());
            Assert.assertEquals("No DataSourceFactory service found for osgi.jdbc.driver.class=org.h2.Driver", e.getReason());
        }
        c.verify();
        
    }

}
