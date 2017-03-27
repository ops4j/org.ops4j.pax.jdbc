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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import javax.sql.DataSource;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.jasypt.encryption.StringEncryptor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.jdbc.config.impl.tracker.TrackerCallback;
import org.ops4j.pax.jdbc.hook.PreHook;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.jdbc.DataSourceFactory;

@SuppressWarnings({
                   "rawtypes", "unchecked"
})
public class DataSourceConfigManagerTest {

    private static final String H2_DSF_FILTER = "(&(objectClass=org.osgi.service.jdbc.DataSourceFactory)(osgi.jdbc.driver.class=org.h2.Driver))";
    private static final String TESTPID = "testpid";
    private static final String H2_DRIVER_CLASS = "org.h2.Driver";
    protected TrackerCallback callback;
    private IMocksControl c;
    private BundleContext context;

    @Before
    public void setup() {
        c = EasyMock.createControl();
        context = c.createMock(BundleContext.class);
    }

    @Test
    public void testUpdatedAndDeleted() throws Exception {
        DataSourceFactory dsf = expectTracked(c, context, DataSourceFactory.class, H2_DSF_FILTER);
        DataSource ds = expectDataSourceCreated(dsf);
        ServiceRegistration sreg = expectRegistration(ds);

        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(DataSourceRegistration.JNDI_SERVICE_NAME, "test");
        properties.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, H2_DRIVER_CLASS);
        properties.put(DataSourceFactory.JDBC_DATABASE_NAME, "mydbname");

        DataSourceConfigManager dsManager = new DataSourceConfigManager(context);

        // Test config created
        c.replay();
        dsManager.updated(TESTPID, properties);

        c.verify();

        c.reset();

        context.removeServiceListener(EasyMock.anyObject(ServiceListener.class));
        expectLastCall().atLeastOnce();
        sreg.unregister();
        expectLastCall();
        expect(context.ungetService(EasyMock.anyObject(ServiceReference.class))).andReturn(true).atLeastOnce();
        // Test config removed
        c.replay();
        dsManager.updated(TESTPID, null);
        
        c.verify();
    }
    
    @Test
    public void testPreHook() throws Exception {
        DataSourceFactory dsf = expectTracked(c, context, DataSourceFactory.class, H2_DSF_FILTER);
        DataSource ds = expectDataSourceCreated(dsf);
        ServiceRegistration sreg = expectRegistration(ds);
        PreHook preHook = expectTracked(c, context, PreHook.class, "(name=myhook)");
        preHook.prepare(EasyMock.anyObject(DataSource.class));
        EasyMock.expectLastCall().once();

        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(DataSourceRegistration.JNDI_SERVICE_NAME, "test");
        properties.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, H2_DRIVER_CLASS);
        properties.put(DataSourceFactory.JDBC_DATABASE_NAME, "mydbname");
        properties.put(PreHook.CONFIG_KEY_NAME, "myhook");

        DataSourceConfigManager dsManager = new DataSourceConfigManager(context);

        // Test config created
        c.replay();
        dsManager.updated(TESTPID, properties);

        c.verify();

        c.reset();

        context.removeServiceListener(EasyMock.anyObject(ServiceListener.class));
        expectLastCall().atLeastOnce();
        sreg.unregister();
        expectLastCall();
        expect(context.ungetService(EasyMock.anyObject(ServiceReference.class))).andReturn(true).atLeastOnce();
        // Test config removed
        c.replay();
        dsManager.updated(TESTPID, null);
        
        c.verify();
    }

    @Test
    public void testNotEnoughInfoToFindDriver() {
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put("other", "value");
        DataSourceConfigManager dsManager = new DataSourceConfigManager(context);

        c.replay();
        try {
            dsManager.updated(TESTPID, properties);
        } catch (ConfigurationException e) {
            assertEquals("Could not determine driver to use. "
                         + "Specify either osgi.jdbc.driver.class or osgi.jdbc.driver.name", e.getReason());
        }
        c.verify();
    }

    @Test
    public void testEncryptor() throws Exception {
        final DataSourceFactory dsf = expectTracked(c, context, DataSourceFactory.class, H2_DSF_FILTER);
        DataSource ds = c.createMock(DataSource.class);
        Capture<Properties> capturedProps = EasyMock.newCapture();
        expect(dsf.createDataSource(EasyMock.capture(capturedProps))).andReturn(ds);
        expectRegistration(ds);
        
        StringEncryptor encryptor = expectTracked(c, context, StringEncryptor.class, "(objectClassName=org.jasypt.encryption.StringEncryptor)");
        expect(encryptor.decrypt("ciphertext")).andReturn("password");
        
        DataSourceConfigManager dsManager = new DataSourceConfigManager(context);

        // Test config created
        c.replay();
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(DataSourceRegistration.JNDI_SERVICE_NAME, "test");
        properties.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, H2_DRIVER_CLASS);
        properties.put(DataSourceFactory.JDBC_DATABASE_NAME, "mydbname");
        properties.put(DataSourceFactory.JDBC_PASSWORD, "ENC(ciphertext)");
        dsManager.updated(TESTPID, properties);
        c.verify();

        // the encrypted value is still encrypted
        Assert.assertEquals("ENC(ciphertext)", properties.get(DataSourceFactory.JDBC_PASSWORD));
        
        Assert.assertEquals("password", capturedProps.getValue().get(DataSourceFactory.JDBC_PASSWORD));
    }

    @Test
    public void testEncryptorWithExternalSecret() throws Exception {
        final DataSourceFactory dsf = expectTracked(c, context, DataSourceFactory.class, H2_DSF_FILTER);
        DataSource ds = expectDataSourceCreated(dsf);
        expectRegistration(ds);
        DataSourceConfigManager dsManager = new DataSourceConfigManager(context);
        StringEncryptor encryptor = expectTracked(c, context, StringEncryptor.class, "(objectClassName=org.jasypt.encryption.StringEncryptor)");
        expect(encryptor.decrypt("ciphertext")).andReturn("password");

        // Test config created
        c.replay();

        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(DataSourceRegistration.JNDI_SERVICE_NAME, "test");
        properties.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, H2_DRIVER_CLASS);
        properties.put(DataSourceFactory.JDBC_DATABASE_NAME, "mydbname");
        String externalEncryptedValue = "FILE(" + ExternalConfigLoaderTest
            .createExternalSecret("ENC(ciphertext)") + ")";
        properties.put(DataSourceFactory.JDBC_PASSWORD, externalEncryptedValue);
        dsManager.updated(TESTPID, properties);
        c.verify();

        // the encrypted/external value is still encrypted/external
        Assert.assertEquals(externalEncryptedValue, properties.get(DataSourceFactory.JDBC_PASSWORD));
    }

    /**
     * Tests: - hidden properties (starting with a dot) are not added to service registry. - nonlocal
     * properties (containing a dot) are not propagated to
     * {@link DataSourceFactory#createDataSource(Properties)}. - local properties (not containing a dot) are
     * propagated to {@link DataSourceFactory#createDataSource(Properties)}.
     *
     * @throws Exception
     */
    @Test
    public void testHiddenAndPropagation() throws Exception {
        final DataSourceFactory dsf = expectTracked(c, context, DataSourceFactory.class, H2_DSF_FILTER);

        final String KEY_HIDDEN_JDBC_PASSWORD = "." + DataSourceFactory.JDBC_PASSWORD;
        final String KEY_NONLOCAL_PROPERTY = "nonlocal.property";
        final String KEY_LOCAL_PROPERTY = "localproperty";
        final String KEY_DATASOURCE_TYPE = "dataSourceType";
        final String KEY_POOL_PROPERTY = "pool.maxTotal";
        final String KEY_FACTORY_PROPERTY = "factory.poolStatements";
        final String VALUE_LOCAL_PROPERTY = "something2";
        final String dbname = "mydbname";
        final String password = "thepassword";
        final String user = "theuser";
        final String poolMaxTotal = "10";
        final String factoryPoolStatements = "true";

        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, H2_DRIVER_CLASS);
        properties.put(DataSourceFactory.JDBC_DATABASE_NAME, dbname);
        properties.put(DataSourceFactory.JDBC_USER, user);
        properties.put(KEY_HIDDEN_JDBC_PASSWORD, password);
        properties.put(KEY_LOCAL_PROPERTY, VALUE_LOCAL_PROPERTY);
        properties.put(KEY_NONLOCAL_PROPERTY, "something");
        properties.put(KEY_POOL_PROPERTY, poolMaxTotal);
        properties.put(KEY_FACTORY_PROPERTY, factoryPoolStatements);

        // Exceptions local properties not being forwarded
        final String VALUE_DATASOURCE_NAME = "myDataSource";
        properties.put(KEY_DATASOURCE_TYPE, "DataSource");
        properties.put(DataSourceFactory.JDBC_DATASOURCE_NAME, VALUE_DATASOURCE_NAME);

        Properties expectedDataSourceProperties = new Properties();
        expectedDataSourceProperties.put(DataSourceFactory.JDBC_DATABASE_NAME, dbname);
        expectedDataSourceProperties.put(DataSourceFactory.JDBC_USER, user);
        expectedDataSourceProperties.put(DataSourceFactory.JDBC_PASSWORD, password);
        expectedDataSourceProperties.put(KEY_LOCAL_PROPERTY, VALUE_LOCAL_PROPERTY);
        expectedDataSourceProperties.put(KEY_POOL_PROPERTY, poolMaxTotal);
        expectedDataSourceProperties.put(KEY_FACTORY_PROPERTY, factoryPoolStatements);

        
        DataSource ds = expectDataSourceCreated(dsf);

        Hashtable<String, String> expectedServiceProperties = (Hashtable<String, String>)properties.clone();
        expectedServiceProperties.remove(KEY_HIDDEN_JDBC_PASSWORD);
        expectedServiceProperties.put("osgi.jndi.service.name", VALUE_DATASOURCE_NAME);
        ServiceRegistration sreg = c.createMock(ServiceRegistration.class);
        expect(context.registerService(anyString(), eq(ds), eq(expectedServiceProperties))).andReturn(sreg);

        DataSourceConfigManager dsManager = new DataSourceConfigManager(context);

        // Test config created
        c.replay();
        dsManager.updated(TESTPID, properties);
        c.verify();
    }

    private <T> T expectTracked(IMocksControl c, BundleContext context, Class<T> iface, String expectedFilter)
        throws InvalidSyntaxException {
        final T serviceMock = c.createMock(iface);
        ServiceReference ref = c.createMock(ServiceReference.class);
        context.addServiceListener(EasyMock.anyObject(ServiceListener.class), EasyMock.eq(expectedFilter));
        expectLastCall();
        ServiceReference[] refs = new ServiceReference[] {
                                                          ref
        };
        expect(context.getServiceReferences((String)null, expectedFilter)).andReturn(refs);
        expect(context.getService(ref)).andReturn(serviceMock);
        return serviceMock;
    }
    
    private DataSource expectDataSourceCreated(final DataSourceFactory dsf) throws SQLException {
        DataSource ds = c.createMock(DataSource.class);
        expect(dsf.createDataSource(EasyMock.anyObject(Properties.class))).andReturn(ds);
        return ds;
    }

    private ServiceRegistration expectRegistration(DataSource ds) {
        ServiceRegistration sreg = c.createMock(ServiceRegistration.class);
        expect(context.registerService(anyString(), eq(ds), anyObject(Dictionary.class))).andReturn(sreg);
        return sreg;
    }

}
