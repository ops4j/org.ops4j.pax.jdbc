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

import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class DataSourcePublisher {
    static final String DATASOURCE_TYPE = "dataSourceType";
    private static final String JNDI_SERVICE_NAME = "osgi.jndi.service.name";
    
    // These config keys will not be forwarded to the DataSourceFactory
    private static final String[] NOT_FORWARDED_KEYS = {
        "service.pid",
        DataSourceFactory.OSGI_JDBC_DRIVER_CLASS,
        DataSourceFactory.OSGI_JDBC_DRIVER_NAME,
        DataSourceFactory.JDBC_DATASOURCE_NAME,
        "service.factoryPid",
        "felix.fileinstall.filename",
        "aries.managed",
        JNDI_SERVICE_NAME,
        DATASOURCE_TYPE
    };
    private Logger LOG = LoggerFactory.getLogger(DataSourcePublisher.class);
    private Set<String> ignoredKeys;

    /**
     * Map from pid + extension to Closeable which holds the object to close for e.g. a DataSource
     */
    private Collection<Closeable> closeables;

    /**
     * Map from pid + extension to ServiceRegistration
     */
    private Collection<ServiceRegistration> serviceRegs;
    private BundleContext context;
    private Dictionary config;

    public DataSourcePublisher(BundleContext context, final Dictionary config) {
        this.context = context;
        this.config = config;
        Object dsName = this.config.get(DataSourceFactory.JDBC_DATASOURCE_NAME);
        if (dsName != null && this.config.get(JNDI_SERVICE_NAME) == null) {
            this.config.put(JNDI_SERVICE_NAME, dsName);
        }
        this.ignoredKeys = new HashSet<String>(Arrays.asList(NOT_FORWARDED_KEYS));
        this.closeables = new ArrayList<Closeable>();
        this.serviceRegs = new ArrayList<ServiceRegistration>();
    }

    public void publish(DataSourceFactory dsf) {
        String typeName = (String) config.get(DATASOURCE_TYPE);
        Class<?> type = getType(typeName);
        try {
            Object ds = createDs(dsf, type);
            if (ds instanceof Closeable) {
                closeables.add((Closeable) ds);
            }
            ServiceRegistration reg = context.registerService(type.getName(), ds, config);
            serviceRegs.add(reg);
        }
        catch (SQLException e) {
            LOG.warn(e.getMessage(), e);
        }
    }
    
    private Class<?> getType(String typeName) {
        if (typeName == null || DataSource.class.getSimpleName().equals(typeName)) {
            return DataSource.class;
        }  else if (ConnectionPoolDataSource.class.getSimpleName().equals(typeName)) {
            return ConnectionPoolDataSource.class;
        } else if (XADataSource.class.getSimpleName().equals(typeName)) { 
            return XADataSource.class;
        } else {
            throw new IllegalArgumentException("Problem in DataSource config : " + DATASOURCE_TYPE + " must be one of "
                + DataSource.class.getSimpleName() + ","
                + ConnectionPoolDataSource.class.getSimpleName() + ","
                + XADataSource.class.getSimpleName());
        }
    }

    private Object createDs(DataSourceFactory dsf, Class<?> type) throws SQLException {
        Properties props = toProperties(config);
        if (type == DataSource.class) {
            return dsf.createDataSource(props);
        } else if (type == ConnectionPoolDataSource.class) {
            return dsf.createConnectionPoolDataSource(props);
        } else {
            return dsf.createXADataSource(props);
        }
    }

    private Properties toProperties(Dictionary dict) {
        Properties props = new Properties();
        Enumeration keys = dict.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            if (!ignoredKeys.contains(key)) {
                props.put(key, dict.get(key));
            }
        }
        return props;
    }

    public void unpublish() {
        for (ServiceRegistration reg : serviceRegs) {
            reg.unregister();
        }
        for (Closeable closeable : closeables) {
            safeClose(closeable);
        }
    }

    private void safeClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            }
            catch (IOException e) {
                LOG.warn("Error closing " + closeable.getClass() + ": " + e.getMessage(), e);
            }
        }
    }
}
