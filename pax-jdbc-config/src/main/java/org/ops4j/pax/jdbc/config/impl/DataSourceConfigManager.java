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

import static org.osgi.framework.FrameworkUtil.createFilter;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jasypt.encryption.StringEncryptor;
import org.ops4j.pax.jdbc.config.impl.tracker.MultiServiceTracker;
import org.ops4j.pax.jdbc.config.impl.tracker.TrackerCallback;
import org.ops4j.pax.jdbc.pool.common.PooledDataSourceFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Watches for DataSource configs in OSGi configuration admin and creates / destroys the trackers
 * for the DataSourceFactories and pooling support
 */
@SuppressWarnings({ "rawtypes"})
public class DataSourceConfigManager implements ManagedServiceFactory {

    private final class TrackerCallbackImpl implements TrackerCallback {
        private final Dictionary<String, Object> config;

        private TrackerCallbackImpl(Dictionary<String, Object> config) {
            this.config = config;
        }

        @Override
        public Closeable activate(MultiServiceTracker tracker) {
            StringEncryptor decryptor = tracker.getService(StringEncryptor.class);
            Dictionary<String, Object> decryptedConfig = new Decryptor(decryptor).decrypt(config);
            PooledDataSourceFactory pdsf = tracker.getService(PooledDataSourceFactory.class);
            DataSourceFactory dsf = tracker.getService(DataSourceFactory.class);
            DataSourceFactory actualDsf = pdsf != null ? new PoolingWrapper(pdsf, dsf) : dsf;
            return new DataSourceRegistration(context, actualDsf, config, decryptedConfig);
        }
    }

    private Logger LOG = LoggerFactory.getLogger(DataSourceConfigManager.class);
    BundleContext context;

    /**
     * Stores one ServiceTracker for DataSourceFactories for each config pid
     */
    private Map<String, MultiServiceTracker> trackers;

    public DataSourceConfigManager(BundleContext context) {
        this.context = context;
        this.trackers = new HashMap<String, MultiServiceTracker>();
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

        try {
            Dictionary<String, Object> loadedConfig = new ExternalConfigLoader().resolve(config);
            final MultiServiceTracker tracker = createTracker(new TrackerCallbackImpl(loadedConfig));
            Filter dsfFilter = getDSFFilter(loadedConfig);
            Filter pdsfFilter = getPooledDSFFilter(loadedConfig);
            if (Decryptor.isEncrypted(loadedConfig)) {
                tracker.track(StringEncryptor.class, getAliasFilter(loadedConfig));
            }
            if (pdsfFilter != null) {
                tracker.track(PooledDataSourceFactory.class, pdsfFilter);
            }
            tracker.track(DataSourceFactory.class, dsfFilter);
            tracker.open();
            trackers.put(pid, tracker);
        }
        catch (InvalidSyntaxException e) {
            LOG.warn("Invalid filter for DataSource config from pid " + pid, e);
        }
    }

    private Filter getAliasFilter(Dictionary<String, Object> loadedConfig) throws InvalidSyntaxException {
        String alias = Decryptor.getAlias(loadedConfig);
        String objectClassName = "(objectClassName=" + StringEncryptor.class.getName() + ")";
        return alias == null ? createFilter(objectClassName) : createFilter("(&" + objectClassName + "(alias=" + alias + "))");
    }
    
    MultiServiceTracker createTracker(TrackerCallback callback) {
        return new MultiServiceTracker(context, callback);
    } 

    private Filter getPooledDSFFilter(Dictionary config)
        throws ConfigurationException, InvalidSyntaxException {
        String pool = (String)config.remove(PooledDataSourceFactory.POOL_KEY);
        boolean isXa = isXa(config);
        if (pool == null) {
            if (isXa) {
                throw new ConfigurationException(null, "Can not create XA DataSource without pooling.");
            } else {
                return null;
            }
        }
        
        List<String> filterList = new ArrayList<String>();
        filterList.add("objectClass=" + PooledDataSourceFactory.class.getName());
        filterList.add("pool=" + pool);
        filterList.add("xa=" + isXa);
        String filter = andFilter(filterList);
        return createFilter(filter);
    }
    
    private boolean isXa(Dictionary config) throws ConfigurationException {
        String xa = (String) config.remove(PooledDataSourceFactory.XA_KEY);
        if (xa == null) {
            return false;
        }
        if (!"true".equals(xa)) {
            throw new ConfigurationException(null, "XA can only be set to true");
        }
        return true;
    }

    private Filter getDSFFilter(Dictionary config) throws ConfigurationException, InvalidSyntaxException {
        String driverClass = (String) config.get(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS);
        String driverName = (String) config.get(DataSourceFactory.OSGI_JDBC_DRIVER_NAME);
        if (driverClass == null && driverName == null) {
            throw new ConfigurationException(null,
                "Could not determine driver to use. Specify either "
                    + DataSourceFactory.OSGI_JDBC_DRIVER_CLASS + " or "
                    + DataSourceFactory.OSGI_JDBC_DRIVER_NAME);
        }
        List<String> filterList = new ArrayList<String>();
        filterList.add("objectClass=" + DataSourceFactory.class.getName());
        if (driverClass != null) {
            filterList.add(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS + "=" + driverClass);
        }
        if (driverName != null) {
            filterList.add(DataSourceFactory.OSGI_JDBC_DRIVER_NAME + "=" + driverName);
        }
        String filter = andFilter(filterList);
        
        return createFilter(filter);
    }

    private String andFilter(List<String> filterList) {
        StringBuilder filter = new StringBuilder();
        if (filterList.size() > 1) {
            filter.append("(&");
        }
        for (String filterPart : filterList) {
            filter.append("(" + filterPart + ")");
        }
        if (filterList.size() > 1) {
            filter.append(")");
        }
        return filter.toString();
    }

    @Override
    public void deleted(String pid) {
        MultiServiceTracker tracker = trackers.get(pid);
        if (tracker != null) {
            tracker.close();
            trackers.remove(pid);
        }
    }

}
