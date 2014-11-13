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

    private static final String JNDI_SERVICE_NAME = "osgi.jndi.service.name";
    private static String[] IGNORED_KEYS = {
        "service.pid",
        DataSourceFactory.OSGI_JDBC_DRIVER_CLASS,
        DataSourceFactory.OSGI_JDBC_DRIVER_NAME,
        DataSourceFactory.JDBC_DATASOURCE_NAME,
        "service.factoryPid",
        "felix.fileinstall.filename",
        JNDI_SERVICE_NAME };
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
        this.ignoredKeys = new HashSet<String>(Arrays.asList(IGNORED_KEYS));
        this.closeables = new ArrayList<Closeable>();
        this.serviceRegs = new ArrayList<ServiceRegistration>();
    }

    public void publish(DataSourceFactory dsf) {
        publishDataSource(dsf);
        publishConnectionPoolDataSource(dsf);
        publishXADataSource(dsf);
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

    private void publishDataSource(DataSourceFactory dsf) {
        try {
            DataSource ds = dsf.createDataSource(toProperties(config));
            if (ds instanceof Closeable) {
                closeables.add((Closeable) ds);
            }
            ServiceRegistration reg = context.registerService(DataSource.class.getName(), ds,
                config);
            serviceRegs.add(reg);
        }
        catch (SQLException e) {
            LOG.warn("Error creating DataSource. " + e.getMessage(), e);
        }
    }

    private void publishConnectionPoolDataSource(DataSourceFactory dsf) {
        try {
            ConnectionPoolDataSource ds = dsf.createConnectionPoolDataSource(toProperties(config));
            ServiceRegistration reg = context.registerService(
                ConnectionPoolDataSource.class.getName(), ds, config);
            serviceRegs.add(reg);
        }
        catch (SQLException e) {
            LOG.debug("Error creating ConnectionPoolDataSource. " + e.getMessage(), e);
        }
    }

    private void publishXADataSource(DataSourceFactory dsf) {
        try {
            XADataSource ds = dsf.createXADataSource(toProperties(config));
            ServiceRegistration reg = context.registerService(XADataSource.class.getName(), ds,
                config);
            serviceRegs.add(reg);
        }
        catch (SQLException e) {
            LOG.debug("Error creating XADataSource. " + e.getMessage(), e);
        }
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
