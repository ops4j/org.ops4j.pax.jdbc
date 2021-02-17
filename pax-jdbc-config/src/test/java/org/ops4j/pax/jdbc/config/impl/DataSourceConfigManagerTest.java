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

import org.jasypt.encryption.StringEncryptor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ops4j.pax.jdbc.config.ConfigLoader;
import org.ops4j.pax.jdbc.hook.PreHook;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.jdbc.DataSourceFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataSourceConfigManagerTest {

    private static final String H2_DSF_FILTER = "(&(objectClass=org.osgi.service.jdbc.DataSourceFactory)(osgi.jdbc.driver.class=org.h2.Driver))";
    private static final String TESTPID = "testpid";
    private static final String H2_DRIVER_CLASS = "org.h2.Driver";
    private BundleContext context;

    @Before
    public void setup() throws Exception {
        context = mock(BundleContext.class);
        when(context.createFilter(anyString())).thenAnswer(new Answer<Filter>() {
            @Override
            public Filter answer(InvocationOnMock invocation) throws Throwable {
                return FrameworkUtil.createFilter(invocation.getArgument(0, String.class));
            }
        });
        @SuppressWarnings("unchecked")
        ServiceReference<FileConfigLoader> ref = (ServiceReference<FileConfigLoader>) mock(ServiceReference.class);
        ServiceReference<?>[] refs = new ServiceReference[] { ref };
        String filter = "(" + Constants.OBJECTCLASS + "=" + ConfigLoader.class.getName() + ")";
        when(context.getServiceReferences((String) null, filter)).thenReturn(refs);
        when(context.getService(ref)).thenReturn(new FileConfigLoader());
    }

    @Test
    public void testUpdatedAndDeleted() throws Exception {
        DataSourceFactory dsf = expectTracked(context, DataSourceFactory.class, H2_DSF_FILTER);
        DataSource ds = expectDataSourceCreated(dsf);
        ServiceRegistration<?> sreg = expectRegistration(ds);

        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(DataSourceRegistration.JNDI_SERVICE_NAME, "test");
        properties.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, H2_DRIVER_CLASS);
        properties.put(DataSourceFactory.JDBC_DATABASE_NAME, "mydbname");

        DataSourceConfigManager dsManager = new DataSourceConfigManager(context, new ExternalConfigLoader(context));
        dsManager.updated(TESTPID, properties);

        verify(context).addServiceListener(any(ServiceListener.class), eq(H2_DSF_FILTER));

        reset(dsf, ds, sreg);

        dsManager.updated(TESTPID, null);

        verify(context).removeServiceListener(any(ServiceListener.class));
        verify(sreg).unregister();
        verify(context).ungetService(any(ServiceReference.class));
    }

    @Test
    public void testPreHook() throws Exception {
        DataSourceFactory dsf = expectTracked(context, DataSourceFactory.class, H2_DSF_FILTER);
        DataSource ds = expectDataSourceCreated(dsf);
        ServiceRegistration<?> sreg = expectRegistration(ds);
        PreHook preHook = expectTracked(context, PreHook.class, "(&(objectClass=org.ops4j.pax.jdbc.hook.PreHook)(name=myhook))");

        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(DataSourceRegistration.JNDI_SERVICE_NAME, "test");
        properties.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, H2_DRIVER_CLASS);
        properties.put(DataSourceFactory.JDBC_DATABASE_NAME, "mydbname");
        properties.put(PreHook.CONFIG_KEY_NAME, "myhook");

        DataSourceConfigManager dsManager = new DataSourceConfigManager(context, new ExternalConfigLoader(context));

        dsManager.updated(TESTPID, properties);

        verify(preHook).prepare(any(DataSource.class));

        reset(dsf, ds, sreg);

        dsManager.updated(TESTPID, null);

        verify(context, atLeastOnce()).removeServiceListener(any(ServiceListener.class));
        verify(sreg, atLeastOnce()).unregister();
        verify(context, atLeastOnce()).ungetService(any(ServiceReference.class));
    }

    @Test
    public void testNotEnoughInfoToFindDriver() {
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put("other", "value");

        DataSourceConfigManager dsManager = new DataSourceConfigManager(context, new ExternalConfigLoader(context));

        try {
            dsManager.updated(TESTPID, properties);
            fail();
        } catch (ConfigurationException e) {
            assertEquals("Could not determine driver to use. "
                    + "Specify either osgi.jdbc.driver.class or osgi.jdbc.driver.name", e.getReason());
        }
    }

    @Test
    public void testEncryptor() throws Exception {
        final DataSourceFactory dsf = expectTracked(context, DataSourceFactory.class, H2_DSF_FILTER);
        DataSource ds = mock(DataSource.class);
        ArgumentCaptor<Properties> capturedProps = ArgumentCaptor.forClass(Properties.class);
        when(dsf.createDataSource(capturedProps.capture())).thenReturn(ds);
        expectRegistration(ds);

        StringEncryptor encryptor = expectTracked(context, StringEncryptor.class, "(objectClass=org.jasypt.encryption.StringEncryptor)");
        when(encryptor.decrypt("ciphertext")).thenReturn("password");

        DataSourceConfigManager dsManager = new DataSourceConfigManager(context, new ExternalConfigLoader(context));

        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(DataSourceRegistration.JNDI_SERVICE_NAME, "test");
        properties.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, H2_DRIVER_CLASS);
        properties.put(DataSourceFactory.JDBC_DATABASE_NAME, "mydbname");
        properties.put(DataSourceFactory.JDBC_PASSWORD, "ENC(ciphertext)");
        dsManager.updated(TESTPID, properties);

        // the encrypted value is still encrypted
        assertEquals("ENC(ciphertext)", properties.get(DataSourceFactory.JDBC_PASSWORD));

        assertEquals("password", capturedProps.getValue().get(DataSourceFactory.JDBC_PASSWORD));
    }

    @Test
    public void testEncryptorWithExternalSecret() throws Exception {
        final DataSourceFactory dsf = expectTracked(context, DataSourceFactory.class, H2_DSF_FILTER);
        DataSource ds = expectDataSourceCreated(dsf);
        expectRegistration(ds);
        StringEncryptor encryptor = expectTracked(context, StringEncryptor.class, "(objectClass=org.jasypt.encryption.StringEncryptor)");
        when(encryptor.decrypt("ciphertext")).thenReturn("password");

        DataSourceConfigManager dsManager = new DataSourceConfigManager(context, new ExternalConfigLoader(context));

        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(DataSourceRegistration.JNDI_SERVICE_NAME, "test");
        properties.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, H2_DRIVER_CLASS);
        properties.put(DataSourceFactory.JDBC_DATABASE_NAME, "mydbname");
        String externalEncryptedValue = "FILE(" + ExternalConfigLoaderTest
                .createExternalSecret("ENC(ciphertext)") + ")";
        properties.put(DataSourceFactory.JDBC_PASSWORD, externalEncryptedValue);
        dsManager.updated(TESTPID, properties);

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
        final DataSourceFactory dsf = expectTracked(context, DataSourceFactory.class, H2_DSF_FILTER);

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

        @SuppressWarnings("unchecked")
        Hashtable<String, String> expectedServiceProperties = (Hashtable<String, String>) properties.clone();
        expectedServiceProperties.remove(keyHiddenJdbcPassword);
        expectedServiceProperties.put("osgi.jndi.service.name", valueDatasourceName);
        ServiceRegistration<?> sreg = mock(ServiceRegistration.class);

        DataSourceConfigManager dsManager = new DataSourceConfigManager(context, new ExternalConfigLoader(context));

        dsManager.updated(TESTPID, properties);
        verify(context).registerService(anyString(), eq(ds), eq(expectedServiceProperties));
    }

    private <T> T expectTracked(BundleContext context, Class<T> iface, String expectedFilter) throws InvalidSyntaxException {
        final T serviceMock = mock(iface);
        @SuppressWarnings("unchecked")
        ServiceReference<T> ref = (ServiceReference<T>) mock(ServiceReference.class);
        ServiceReference<?>[] refs = new ServiceReference[] { ref };
        when(context.getServiceReferences((String) null, expectedFilter)).thenReturn(refs);
        when(context.getService(ref)).thenReturn(serviceMock);
        return serviceMock;
    }

    private DataSource expectDataSourceCreated(final DataSourceFactory dsf) throws SQLException {
        DataSource ds = mock(DataSource.class);
        when(dsf.createDataSource(any(Properties.class))).thenReturn(ds);
        return ds;
    }

    @SuppressWarnings("unchecked")
    private ServiceRegistration<DataSource> expectRegistration(DataSource ds) {
        ServiceRegistration<DataSource> sreg = mock(ServiceRegistration.class);
        when(context.registerService(anyString(), eq(ds), any(Dictionary.class))).thenReturn(sreg);
        return sreg;
    }

}
