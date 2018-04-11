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

import java.io.Closeable;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.ops4j.pax.jdbc.hook.PreHook;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({
    "rawtypes", "unchecked"
})
public class DataSourceRegistration implements Closeable {

    static final String DATASOURCE_TYPE = "dataSourceType";
    static final String JNDI_SERVICE_NAME = "osgi.jndi.service.name";

    // By default all local keys (without a dot) are forwarded to the DataSourceFactory.
    // These config keys will explicitly not be forwarded to the DataSourceFactory
    // (even though they are "local" keys without a dot ".")
    // Exception: for pooling support keys with prefix pool or factory are always
    // forwarded.
    private static final Set<String> NOT_FORWARDED_KEYS = new HashSet<String>(Arrays.asList(
            DataSourceFactory.JDBC_DATASOURCE_NAME,
            DATASOURCE_TYPE
    ));
    private static final Set<String> FORWARDED_KEY_PREFIXES = new HashSet<>(Arrays.asList(
            "pool.",
            "factory."
    ));
    // additionally all keys prefixed with "jdbc." will be forwarded (with the prefix stripped).
    private static final String CONFIG_KEY_PREFIX = "jdbc.";
    
    private static final Logger LOG = LoggerFactory.getLogger(DataSourceRegistration.class);

    private AutoCloseable dataSource;
    private ServiceRegistration serviceReg;

    public DataSourceRegistration(BundleContext context, DataSourceFactory dsf, final Dictionary config, final Dictionary decryptedConfig, final PreHook preHook) {
        String dsName = getDSName(config);
        if (dsName != null) {
            config.put(JNDI_SERVICE_NAME, dsName);
        }
        try {
            LOG.info("Found DataSourceFactory. Creating DataSource {}", dsName);
            String typeName = (String)config.get(DATASOURCE_TYPE);
            Class<?> type = getType(typeName);
            Object ds = createDs(dsf, type, decryptedConfig);
            if (ds instanceof AutoCloseable) {
                dataSource = (AutoCloseable)ds;
            }
            if (preHook != null && ds instanceof DataSource) {
                LOG.info("Executing pre hook for DataSource {}", dsName);
                preHook.prepare((DataSource)ds);
                LOG.info("Pre hook finished. Publishing DataSource {}", dsName);
            }
            serviceReg = context.registerService(type.getName(), ds, filterHidden(config));
        } catch (SQLException e) {
            LOG.warn(e.getMessage(), e);
        }
    }

    static String getDSName(Dictionary config) {
        String jndiName = (String)config.get(DataSourceRegistration.JNDI_SERVICE_NAME);
        String dsName = (String)config.get(DataSourceFactory.JDBC_DATASOURCE_NAME);
        if (dsName == null && jndiName == null) {
            throw new IllegalStateException("Can not determine DataSource name. Must set " + DataSourceRegistration.JNDI_SERVICE_NAME + " or " + DataSourceFactory.JDBC_DATASOURCE_NAME);
        }
        return jndiName != null ? jndiName : dsName; 
    }

    @Override
    public void close() {
        if (serviceReg != null) {
            serviceReg.unregister();
        }
        safeClose(dataSource);
    }

    private Class<?> getType(String typeName) {
        if (typeName == null || DataSource.class.getSimpleName().equals(typeName)) {
            return DataSource.class;
        } else if (ConnectionPoolDataSource.class.getSimpleName().equals(typeName)) {
            return ConnectionPoolDataSource.class;
        } else if (XADataSource.class.getSimpleName().equals(typeName)) {
            return XADataSource.class;
        } else {
            String msg = String.format("Problem in DataSource config : %s must be one of %s , %s, %s",
                DATASOURCE_TYPE, //
                DataSource.class.getSimpleName(), //
                ConnectionPoolDataSource.class.getSimpleName(), //
                XADataSource.class.getSimpleName());
            throw new IllegalArgumentException(msg);
        }
    }

    private Object createDs(DataSourceFactory dsf, Class<?> type, Dictionary decryptedConfig) throws SQLException {
        Objects.requireNonNull(dsf, "Must provide a DataSourceFactory");
        Properties props = toProperties(decryptedConfig);
        if (type == DataSource.class) {
            addDataSourceName(dsf, decryptedConfig, props);
            return dsf.createDataSource(props);
        } else if (type == ConnectionPoolDataSource.class) {
            return dsf.createConnectionPoolDataSource(props);
        } else {
            return dsf.createXADataSource(props);
        }
    }

    /**
     * Add dataSourceName for dbcp2 pooled DS to configure JMX bean name
     * @param dsf 
     * @param config
     * @param props
     */
    private void addDataSourceName(DataSourceFactory dsf, Dictionary config, Properties props) {
        Class<? extends DataSourceFactory> dsfClass = dsf.getClass();
        if (dsfClass != null && dsfClass.getName().startsWith("org.ops4j.pax.jdbc.pool.dbcp2")) {
            props.put(DataSourceFactory.JDBC_DATASOURCE_NAME,
                      config.get(DataSourceFactory.JDBC_DATASOURCE_NAME));
        }
    }

    private Properties toProperties(Dictionary dict) {
        Properties props = new Properties();
        Enumeration keys = dict.keys();
        while (keys.hasMoreElements()) {
            final String originalKey = (String) keys.nextElement();
            final String unhiddenKey = unhide(originalKey);
            if (shouldForwardToDataSourceFactory(unhiddenKey)) {
                props.put(unhiddenKey, dict.get(originalKey));
            } else if (unhiddenKey.startsWith(CONFIG_KEY_PREFIX)) {
                props.put(unhiddenKey.substring(CONFIG_KEY_PREFIX.length()), dict.get(originalKey));
            }
        }
        return props;
    }

    private boolean shouldForwardToDataSourceFactory(String key) {
        // only forward local configuration keys (i. e. those without a dot)
        // exception: the DATASOURCE_TYPE key (as legacy).
        boolean shouldForward = (!key.contains(".") && !NOT_FORWARDED_KEYS.contains(key));
        for (Iterator<String> it = FORWARDED_KEY_PREFIXES.iterator();
                !shouldForward && it.hasNext(); ) {
            shouldForward = key.startsWith(it.next());
        }
        return shouldForward;
    }

    private Dictionary filterHidden(Dictionary dict) {
        final Dictionary filtered = new Hashtable(dict.size());
        final Enumeration keys = dict.keys();
        while (keys.hasMoreElements()) {
            final String key = (String)keys.nextElement();
            if (!isHidden(key)) {
                filtered.put(key, dict.get(key));
            }
        }
        return filtered;
    }

    private String unhide(String key) {
        return isHidden(key) ? key.substring(1) : key;
    }

    private boolean isHidden(String key) {
        return key != null && key.startsWith(".");
    }

    private void safeClose(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                LOG.warn("Error closing " + closeable.getClass() + ": " + e.getMessage(), e);
            }
        }
    }

}
