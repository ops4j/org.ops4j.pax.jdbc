/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ops4j.pax.jdbc.config.impl;

import org.jasypt.encryption.StringEncryptor;
import org.ops4j.pax.jdbc.hook.PreHook;
import org.ops4j.pax.jdbc.pool.common.PooledDataSourceFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.ServiceTracker;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Watches for DataSource configs in OSGi configuration admin and creates / destroys the trackers
 * for the DataSourceFactories and pooling support
 */
@SuppressWarnings({"rawtypes"})
public class DataSourceConfigManager implements ManagedServiceFactory {

    BundleContext context;

    /**
     * Stores one ServiceTracker for DataSourceFactories for each config pid
     */
    private Map<String, ServiceTracker<?, ?>> trackers;

    public DataSourceConfigManager(BundleContext context) {
        this.context = context;
        this.trackers = new HashMap<>();
    }

    @Override
    public String getName() {
        return "datasource";
    }


    @Override
    public void updated(final String pid, final Dictionary config) throws ConfigurationException {
        deleted(pid);
        if (config == null) {
            return;
        }

        Dictionary<String, Object> loadedConfig = new ExternalConfigLoader().resolve(config);

        String seFilter = getStringEncryptorFilter(loadedConfig);
        String dsfFilter = getDSFFilter(loadedConfig);
        String pdsfFilter = getPooledDSFFilter(loadedConfig);
        String phFilter = getPreHookFilter(loadedConfig);

        ServiceTrackerHelper helper = ServiceTrackerHelper.helper(context);
        ServiceTracker<?, ?> tracker;

        if (Objects.nonNull(pdsfFilter)) {
            tracker = helper.track(StringEncryptor.class, seFilter, se ->
                    helper.track(PooledDataSourceFactory.class, pdsfFilter, pdsf ->
                            helper.track(PreHook.class, phFilter, ph ->
                                    helper.track(DataSourceFactory.class, dsfFilter, dsf ->
                                                    new DataSourceRegistration(context,
                                                            new PoolingWrapper(pdsf, dsf),
                                                            loadedConfig,
                                                            new Decryptor(se).decrypt(loadedConfig),
                                                            ph),
                                            DataSourceRegistration::close))));
        } else {
            tracker = helper.track(StringEncryptor.class, seFilter, se ->
                    helper.track(PreHook.class, phFilter, ph ->
                            helper.track(DataSourceFactory.class, dsfFilter, dsf ->
                                            new DataSourceRegistration(context,
                                                    dsf,
                                                    loadedConfig,
                                                    new Decryptor(se).decrypt(loadedConfig),
                                                    ph),
                                    DataSourceRegistration::close)));
        }
        trackers.put(pid, tracker);
    }

    static String getStringEncryptorFilter(Dictionary<String, Object> config) {
        if (Decryptor.isEncrypted(config)) {
            String alias = Decryptor.getAlias(config);
            return andFilter(eqFilter("objectClass", StringEncryptor.class.getName()),
                    eqFilter("alias", alias));
        }
        return null;
    }

    static String getPreHookFilter(Dictionary<String, Object> config) {
        String preHookName = (String) config.get(PreHook.CONFIG_KEY_NAME);
        if (preHookName != null) {
            return andFilter(eqFilter("objectClass", PreHook.class.getName()),
                    eqFilter(PreHook.KEY_NAME, preHookName));
        }
        return null;
    }

    static String getPooledDSFFilter(Dictionary<String, Object> config) throws ConfigurationException {
        String pool = (String) config.remove(PooledDataSourceFactory.POOL_KEY);
        boolean isXa = isXa(config);
        if (pool == null) {
            if (isXa) {
                throw new ConfigurationException(null, "Can not create XA DataSource without pooling.");
            } else {
                return null;
            }
        }
        return andFilter(eqFilter("objectClass", PooledDataSourceFactory.class.getName()),
                eqFilter("pool", pool),
                eqFilter("xa", Boolean.toString(isXa)));
    }

    static boolean isXa(Dictionary<String, Object> config) throws ConfigurationException {
        String xa = (String) config.remove(PooledDataSourceFactory.XA_KEY);
        if (xa == null) {
            return false;
        } else {
            if ("true".equals(xa)) {
                return true;
            } else if ("false".equals(xa)) {
                return false;
            } else {
                throw new ConfigurationException(null, "Invalid XA configuration provided, XA can only be set to true or false");
            }
        }
    }

    private String getDSFFilter(Dictionary<String, Object> config) throws ConfigurationException {
        String driverClass = (String) config.get(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS);
        String driverName = (String) config.get(DataSourceFactory.OSGI_JDBC_DRIVER_NAME);
        if (driverClass == null && driverName == null) {
            throw new ConfigurationException(null,
                    "Could not determine driver to use. Specify either "
                            + DataSourceFactory.OSGI_JDBC_DRIVER_CLASS + " or "
                            + DataSourceFactory.OSGI_JDBC_DRIVER_NAME);
        }
        return andFilter(eqFilter("objectClass", DataSourceFactory.class.getName()),
                eqFilter(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, driverClass),
                eqFilter(DataSourceFactory.OSGI_JDBC_DRIVER_NAME, driverName));
    }

    static String eqFilter(String key, String value) {
        return value != null ? "(" + key + "=" + value + ")" : null;
    }

    static String andFilter(String... filterList) {
        String last = null;
        StringBuilder filter = new StringBuilder("(&");
        int count = 0;
        for (String filterPart : filterList) {
            if (filterPart != null) {
                last = filterPart;
                filter.append(filterPart);
                count++;
            }
        }
        filter.append(")");

        return count > 1 ? filter.toString() : last;
    }

    @Override
    public void deleted(String pid) {
        ServiceTracker<?, ?> tracker = trackers.remove(pid);
        if (tracker != null) {
            tracker.close();
        }
    }

    synchronized void destroy() {
        for (String pid : trackers.keySet()) {
            deleted(pid);
        }
    }

}
