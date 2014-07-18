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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Watches for DataSource configs in OSGi configuration admin and creates / destroys the
 * respective OSGi services and DataSources
 */
public class DataSourceManager implements ManagedServiceFactory {
    private static String[] IGNORED_KEYS = {"service.pid", 
                                            DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, 
                                            DataSourceFactory.OSGI_JDBC_DRIVER_NAME,
                                            "service.factoryPid",
                                            "felix.fileinstall.filename",
                                            "osgi.jndi.service.name"
                                            };
    private Logger LOG = LoggerFactory.getLogger(DataSourceManager.class);
    private BundleContext context;
    private Set<String> ignoredKeys;
    private Map<String, ServiceRegistration> serviceRegs;
            
    public DataSourceManager(BundleContext context) {
        this.context = context;
        this.ignoredKeys = new HashSet<String>(Arrays.asList(IGNORED_KEYS));
        this.serviceRegs = new HashMap<String, ServiceRegistration>();
    }
    
    protected DataSourceFactory findDSFactoryForDriverClass(String driverClass, String driverName) throws ConfigurationException {
        try {
            if (driverClass == null && driverName == null){
                throw new ConfigurationException(null, "Could not determine driver to use. Specify either " 
                    + DataSourceFactory.OSGI_JDBC_DRIVER_CLASS + " or " + DataSourceFactory.OSGI_JDBC_DRIVER_NAME);
            }
            List<String> filterList = new ArrayList<String>();
            if (driverClass != null)  {
                filterList.add(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS + "=" + driverClass);
            }
            if (driverName != null) {
                filterList.add(DataSourceFactory.OSGI_JDBC_DRIVER_NAME + "=" + driverName);
            }
            String filter = andFilter(filterList);
            ServiceReference[] refs = context.getServiceReferences(DataSourceFactory.class.getName(), filter);
            if (refs == null || refs.length == 0) {
                String msg = "No DataSourceFactory service found for " + DataSourceFactory.OSGI_JDBC_DRIVER_CLASS + "=" + driverClass;
                throw new ConfigurationException(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, msg);
            }
            return (DataSourceFactory)context.getService(refs[0]);
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    private String andFilter(List<String> filterList) {
        StringBuilder filter = new StringBuilder();
        if (filterList.size() > 1) {
            filter.append("&(");
        }
        for (String filterPart : filterList) {
            filter.append("(" + filterPart + ")");
        }
        if (filterList.size() > 1) {
            filter.append(")");
        }
        return filter.toString();
    }

    @SuppressWarnings("rawtypes")
    private Properties toProperties(Dictionary dict) {
        Properties props = new Properties();
        Enumeration keys = dict.keys();
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            if (!ignoredKeys.contains(key)) {
                props.put(key, dict.get(key));
            }
        }
        return props;
    }

    @Override
    public String getName() {
        return "datasource";
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void updated(String pid, Dictionary config) throws ConfigurationException {
        deleted(pid);
        
        if (config == null) {
            return;
        }
        
        String driverClass = (String)config.get(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS);
        String driverName = (String)config.get(DataSourceFactory.OSGI_JDBC_DRIVER_NAME);
        DataSourceFactory dsf = findDSFactoryForDriverClass(driverClass, driverName);
        
        publishDataSource(pid + ".ds", config, dsf);
        publishConnectionPoolDataSource(pid + ".cpds", config, dsf);
        publishXADataSource(pid + ".xads", config, dsf);
    }

    @SuppressWarnings("rawtypes")
    private void publishDataSource(String pid, Dictionary config, DataSourceFactory dsf)
        throws ConfigurationException {
        try {
            DataSource ds = dsf.createDataSource(toProperties(config));
            ServiceRegistration reg = context.registerService(DataSource.class.getName(), ds, config);
            serviceRegs.put(pid, reg);
        } catch (SQLException e) {
            LOG.warn("Error creating DataSource. " + e.getMessage(), e);
        }
    }
    
    @SuppressWarnings("rawtypes")
    private void publishConnectionPoolDataSource(String pid, Dictionary config, DataSourceFactory dsf)
        throws ConfigurationException {
        try {
            ConnectionPoolDataSource ds = dsf.createConnectionPoolDataSource(toProperties(config));
            ServiceRegistration reg = context.registerService(ConnectionPoolDataSource.class.getName(), ds, config);
            serviceRegs.put(pid, reg);
        } catch (SQLException e) {
            LOG.debug("Error creating ConnectionPoolDataSource. " + e.getMessage(), e);
        }
    }
    
    @SuppressWarnings("rawtypes")
    private void publishXADataSource(String pid, Dictionary config, DataSourceFactory dsf)
        throws ConfigurationException {
        try {
            XADataSource ds = dsf.createXADataSource(toProperties(config));
            ServiceRegistration reg = context.registerService(XADataSource.class.getName(), ds, config);
            serviceRegs.put(pid, reg);
        } catch (SQLException e) {
            LOG.debug("Error creating XADataSource. " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleted(String pid) {
        delete(pid + ".ds");
        delete(pid + ".cpds");
        delete(pid + ".xads");
    }

    private void delete(String id) {
        ServiceRegistration reg = serviceRegs.get(id);
        if (reg != null) {
            reg.unregister();
        }
    }

}
