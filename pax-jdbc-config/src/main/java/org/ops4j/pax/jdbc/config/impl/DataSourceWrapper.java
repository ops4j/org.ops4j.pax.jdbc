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

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;
import javax.sql.CommonDataSource;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.jasypt.encryption.StringEncryptor;
import org.ops4j.pax.jdbc.hook.PreHook;
import org.ops4j.pax.jdbc.pool.common.PooledDataSourceFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class registers pooling {@link DataSource} created from
 * {@link org.ops4j.pax.jdbc.pool.common.PooledDataSourceFactory} using provided, existing
 * {@link DataSource}/{@link XADataSource}.
 */
public class DataSourceWrapper {

    public static final Logger LOG = LoggerFactory.getLogger(DataSourceWrapper.class);

    private ServiceTracker<?, ?> tracker;
    private CommonDataSource ds;

    /**
     * A wrapper for data source service registered by application. This wrapper creates pooled {@link javax.sql.DataSource}
     * using properties that are part of original data source registration.
     *
     * @param context {@link BundleContext} of pax-jdbc-config
     * @param externalConfigLoader loader for external configurations
     * @param ds {@link CommonDataSource} instance - application registered and database-specific (as recommended)
     * @param reference {@link CommonDataSource}'s {@link ServiceReference}
     */
    public DataSourceWrapper(BundleContext context, ExternalConfigLoader externalConfigLoader, CommonDataSource ds, ServiceReference<CommonDataSource> reference) {
        LOG.info("Got service reference {}", ds);
        this.ds = ds;

        boolean xa = false;
        Object objectClass = reference.getProperty("objectClass");
        if (objectClass instanceof String) {
            xa = XADataSource.class.getName().equals(objectClass);
        } else if (objectClass instanceof String[]) {
            xa = Arrays.stream((String[]) objectClass).anyMatch(c -> XADataSource.class.getName().equals(c));
        }
        DataSourceFactory providedDataSourceFactory = xa ? new ProvidedDataSourceFactory((XADataSource) ds)
                : new ProvidedDataSourceFactory((DataSource) ds);

        Dictionary<String, Object> config = serviceReferenceProperties(reference);
        Dictionary<String, Object> loadedConfig = externalConfigLoader.resolve(config);
        loadedConfig.put("xa", Boolean.toString(xa));
        loadedConfig.put(Constants.SERVICE_RANKING, getInt(config, Constants.SERVICE_RANKING, 0) + 1000);
        // reference to service being wrapped
        loadedConfig.put("pax.jdbc.service.id.ref", config.get(Constants.SERVICE_ID));

        String seFilter = DataSourceConfigManager.getStringEncryptorFilter(loadedConfig);
        String pdsfFilter = null;
        try {
            pdsfFilter = DataSourceConfigManager.getPooledDSFFilter(loadedConfig);
        } catch (ConfigurationException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        String phFilter = DataSourceConfigManager.getPreHookFilter(loadedConfig);

        ServiceTrackerHelper helper = ServiceTrackerHelper.helper(context);

        if (pdsfFilter == null) {
            throw new IllegalArgumentException("No pooling configuration available for service " + ds.toString()
                    + ": " + loadedConfig);
        }
        final String finalPdsfFilter = pdsfFilter;

        tracker = helper.track(StringEncryptor.class, seFilter, se ->
                helper.track(PooledDataSourceFactory.class, finalPdsfFilter, pdsf ->
                        helper.track(PreHook.class, phFilter, ph ->
                                        new DataSourceRegistration(context,
                                                new PoolingWrapper(pdsf, providedDataSourceFactory),
                                                loadedConfig,
                                                new Decryptor(se).decrypt(loadedConfig),
                                                ph),
                                DataSourceRegistration::close)));
    }

    /**
     * Gets {@link Dictionary} of properties from non-null {@link ServiceReference}
     * @param reference
     * @return
     */
    private Dictionary<String,Object> serviceReferenceProperties(ServiceReference<CommonDataSource> reference) {
        Hashtable<String, Object> result = new Hashtable<>();
        if (reference != null) {
            for (String key : reference.getPropertyKeys()) {
                result.put(key, reference.getProperty(key));
            }
        }
        return result;
    }

    /**
     * Wrapper is closed when the original service is unregistered or if pax-jdbc-config bundle is stopped
     */
    public void close() {
        if (tracker != null) {
            if (ds != null) {
                LOG.info("Closed service reference: {}", this.ds);
            }
            tracker.close();
        }
    }

    private int getInt(Dictionary<String, Object> properties, String name, int defaultValue) {
        Object v = properties.get(name);
        if (v instanceof Integer) {
            return (Integer) v;
        } else if (v instanceof String) {
            return Integer.parseInt((String) v);
        } else {
            return defaultValue;
        }
    }

    /**
     * {@link DataSourceFactory} which doesn't create anything - just returns what it's configured with
     */
    private static final class ProvidedDataSourceFactory implements DataSourceFactory {
        private DataSource providedDataSource;
        private XADataSource providedXADataSource;

        ProvidedDataSourceFactory(DataSource providedDataSource) {
            this.providedDataSource = providedDataSource;
        }

        ProvidedDataSourceFactory(XADataSource providedXADataSource) {
            this.providedXADataSource = providedXADataSource;
        }

        @Override
        public DataSource createDataSource(Properties props) throws SQLException {
            return providedDataSource;
        }

        @Override
        public ConnectionPoolDataSource createConnectionPoolDataSource(Properties props) throws SQLException {
            throw new UnsupportedOperationException("Only javax.sql.DataSource and javax.sql.XADataSource can be created");
        }

        @Override
        public XADataSource createXADataSource(Properties props) throws SQLException {
            return providedXADataSource;
        }

        @Override
        public Driver createDriver(Properties props) throws SQLException {
            throw new UnsupportedOperationException("Only javax.sql.DataSource and javax.sql.XADataSource can be created");
        }
    }

}
