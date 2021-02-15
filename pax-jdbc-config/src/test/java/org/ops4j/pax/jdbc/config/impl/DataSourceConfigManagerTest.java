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

import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import javax.sql.DataSource;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.easymock.IMocksControl;
import org.jasypt.encryption.StringEncryptor;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.jdbc.hook.PreHook;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.jdbc.DataSourceFactory;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.newCapture;
import static org.junit.Assert.assertEquals;
import org.ops4j.pax.jdbc.config.ConfigLoader;
import org.osgi.framework.Constants;

@SuppressWarnings({
                   "rawtypes", "unchecked"
})
public class DataSourceConfigManagerTest {

    private static final String H2_DSF_FILTER = "(&(objectClass=org.osgi.service.jdbc.DataSourceFactory)(osgi.jdbc.driver.class=org.h2.Driver))";
    private static final String TESTPID = "testpid";
    private static final String H2_DRIVER_CLASS = "org.h2.Driver";
    private IMocksControl c;
    private BundleContext context;

    @Before
    public void setup() throws Exception {
        c = EasyMock.createControl();
        context = c.createMock(BundleContext.class);
        Capture<String> capture = newCapture();
        expect(context.createFilter(EasyMock.capture(capture))).andStubAnswer(new IAnswer<Filter>() {
            @Override
            public Filter answer() throws Throwable {
                return FrameworkUtil.createFilter(capture.getValue());
            }
        });
        context.addServiceListener(anyObject(ServiceListener.class), anyString());
        ServiceReference ref = c.createMock(ServiceReference.class);
        ServiceReference[] refs = new ServiceReference[]{ref};
        String filter = "(" + Constants.OBJECTCLASS + "=" + ConfigLoader.class.getName() + ")";
        expect(context.getServiceReferences((String) null, filter)).andReturn(refs);
        expect(context.getService(ref)).andReturn(new FileConfigLoader());
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

        // Test config created
        c.replay();

        DataSourceConfigManager dsManager = new DataSourceConfigManager(context, new ExternalConfigLoader(context));

        dsManager.updated(TESTPID, properties);

        c.verify();

        c.reset();

        context.removeServiceListener(anyObject(ServiceListener.class));
        expectLastCall().atLeastOnce();
        sreg.unregister();
        expectLastCall();
        expect(context.ungetService(anyObject(ServiceReference.class))).andReturn(true).atLeastOnce();
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
        PreHook preHook = expectTracked(c, context, PreHook.class, "(&(objectClass=org.ops4j.pax.jdbc.hook.PreHook)(name=myhook))");
        preHook.prepare(anyObject(DataSource.class));
        expectLastCall().once();

        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(DataSourceRegistration.JNDI_SERVICE_NAME, "test");
        properties.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, H2_DRIVER_CLASS);
        properties.put(DataSourceFactory.JDBC_DATABASE_NAME, "mydbname");
        properties.put(PreHook.CONFIG_KEY_NAME, "myhook");

        // Test config created
        c.replay();

        DataSourceConfigManager dsManager = new DataSourceConfigManager(context, new ExternalConfigLoader(context));

        dsManager.updated(TESTPID, properties);

        c.verify();

        c.reset();

        context.removeServiceListener(anyObject(ServiceListener.class));
        expectLastCall().atLeastOnce();
        sreg.unregister();
        expectLastCall();
        expect(context.ungetService(anyObject(ServiceReference.class))).andReturn(true).atLeastOnce();
        // Test config removed
        c.replay();
        dsManager.updated(TESTPID, null);
        
        c.verify();
    }

    @Test
    public void testNotEnoughInfoToFindDriver() {
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put("other", "value");

        c.replay();

        DataSourceConfigManager dsManager = new DataSourceConfigManager(context, new ExternalConfigLoader(context));

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
        Capture<Properties> capturedProps = newCapture();
        expect(dsf.createDataSource(EasyMock.capture(capturedProps))).andReturn(ds);
        expectRegistration(ds);
        
        StringEncryptor encryptor = expectTracked(c, context, StringEncryptor.class, "(objectClass=org.jasypt.encryption.StringEncryptor)");
        expect(encryptor.decrypt("ciphertext")).andReturn("password");

        // Test config created
        c.replay();

        DataSourceConfigManager dsManager = new DataSourceConfigManager(context, new ExternalConfigLoader(context));

        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(DataSourceRegistration.JNDI_SERVICE_NAME, "test");
        properties.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, H2_DRIVER_CLASS);
        properties.put(DataSourceFactory.JDBC_DATABASE_NAME, "mydbname");
        properties.put(DataSourceFactory.JDBC_PASSWORD, "ENC(ciphertext)");
        dsManager.updated(TESTPID, properties);
        c.verify();

        // the encrypted value is still encrypted
        assertEquals("ENC(ciphertext)", properties.get(DataSourceFactory.JDBC_PASSWORD));
        
        assertEquals("password", capturedProps.getValue().get(DataSourceFactory.JDBC_PASSWORD));
    }

    @Test
    public void testEncryptorWithExternalSecret() throws Exception {
        final DataSourceFactory dsf = expectTracked(c, context, DataSourceFactory.class, H2_DSF_FILTER);
        DataSource ds = expectDataSourceCreated(dsf);
        expectRegistration(ds);
        StringEncryptor encryptor = expectTracked(c, context, StringEncryptor.class, "(objectClass=org.jasypt.encryption.StringEncryptor)");
        expect(encryptor.decrypt("ciphertext")).andReturn("password");

        // Test config created
        c.replay();

        DataSourceConfigManager dsManager = new DataSourceConfigManager(context, new ExternalConfigLoader(context));

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
        assertEquals(externalEncryptedValue, properties.get(DataSourceFactory.JDBC_PASSWORD));
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

        final String keyHiddenJdbcPassword = "." + DataSourceFactory.JDBC_PASSWORD;
        final String keyNonlocalProperty = "nonlocal.property";
        final String keyLocalProperty = "localproperty";
        final String keyDatasourceType = "dataSourceType";
        final String keyPoolProperty = "pool.maxTotal";
        final String keyFactoryProperty = "factory.poolStatements";
        final String valueLocalProperty = "something2";
        final String dbname = "mydbname";
        final String password = "thepassword";
        final String user = "theuser";
        final String poolMaxTotal = "10";
        final String factoryPoolStatements = "true";

        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, H2_DRIVER_CLASS);
        properties.put(DataSourceFactory.JDBC_DATABASE_NAME, dbname);
        properties.put(DataSourceFactory.JDBC_USER, user);
        properties.put(keyHiddenJdbcPassword, password);
        properties.put(keyLocalProperty, valueLocalProperty);
        properties.put(keyNonlocalProperty, "something");
        properties.put(keyPoolProperty, poolMaxTotal);
        properties.put(keyFactoryProperty, factoryPoolStatements);

        // Exceptions local properties not being forwarded
        final String valueDatasourceName = "myDataSource";
        properties.put(keyDatasourceType, "DataSource");
        properties.put(DataSourceFactory.JDBC_DATASOURCE_NAME, valueDatasourceName);
        properties.put(DataSourceRegistration.MANAGED_DATASOURCE, "true");

        Properties expectedDataSourceProperties = new Properties();
        expectedDataSourceProperties.put(DataSourceFactory.JDBC_DATABASE_NAME, dbname);
        expectedDataSourceProperties.put(DataSourceFactory.JDBC_USER, user);
        expectedDataSourceProperties.put(DataSourceFactory.JDBC_PASSWORD, password);
        expectedDataSourceProperties.put(keyLocalProperty, valueLocalProperty);
        expectedDataSourceProperties.put(keyPoolProperty, poolMaxTotal);
        expectedDataSourceProperties.put(keyFactoryProperty, factoryPoolStatements);

        
        DataSource ds = expectDataSourceCreated(dsf);

        Hashtable<String, String> expectedServiceProperties = (Hashtable<String, String>)properties.clone();
        expectedServiceProperties.remove(keyHiddenJdbcPassword);
        expectedServiceProperties.put("osgi.jndi.service.name", valueDatasourceName);
        ServiceRegistration sreg = c.createMock(ServiceRegistration.class);
        expect(context.registerService(anyString(), eq(ds), eq(expectedServiceProperties))).andReturn(sreg);

        // Test config created
        c.replay();

        DataSourceConfigManager dsManager = new DataSourceConfigManager(context, new ExternalConfigLoader(context));

        dsManager.updated(TESTPID, properties);
        c.verify();
    }

    private <T> T expectTracked(IMocksControl c, BundleContext context, Class<T> iface, String expectedFilter)
        throws InvalidSyntaxException {
        final T serviceMock = c.createMock(iface);
        ServiceReference ref = c.createMock(ServiceReference.class);
        context.addServiceListener(anyObject(ServiceListener.class), eq(expectedFilter));
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
        expect(dsf.createDataSource(anyObject(Properties.class))).andReturn(ds);
        return ds;
    }

    private ServiceRegistration expectRegistration(DataSource ds) {
        ServiceRegistration sreg = c.createMock(ServiceRegistration.class);
        expect(context.registerService(anyString(), eq(ds), anyObject(Dictionary.class))).andReturn(sreg);
        return sreg;
    }

}
