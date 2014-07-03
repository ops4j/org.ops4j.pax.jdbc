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
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.jdbc.DataSourceFactory;

public class DataSourceManager implements ManagedServiceFactory {
    private static String[] IGNORED_KEYS = {"service.pid", 
                                            DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, 
                                            DataSourceFactory.OSGI_JDBC_DRIVER_NAME,
                                            "service.factoryPid",
                                            "felix.fileinstall.filename",
                                            "osgi.jndi.service.name"
                                            };
    private BundleContext context;
    private Set<String> ignoredKeys;
    private Map<String, ServiceRegistration> serviceRegs;
            
    public DataSourceManager(BundleContext context) {
        this.context = context;
        this.ignoredKeys = new HashSet<String>(Arrays.asList(IGNORED_KEYS));
        this.serviceRegs = new HashMap<String, ServiceRegistration>();
    }
    
    public void stop() {
    }
    
    protected DataSourceFactory findDSFactoryForDriverClass(String driverClass) throws ConfigurationException {
        try {
            String filter = "(" + DataSourceFactory.OSGI_JDBC_DRIVER_CLASS + "=" + driverClass + ")";
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
        DataSourceFactory dsf = null;
        String driverClass = (String)config.get(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS);
        String driverName = (String)config.get(DataSourceFactory.OSGI_JDBC_DRIVER_NAME);
        if (driverClass != null) {
            dsf = findDSFactoryForDriverClass(driverClass);
        } else if (driverName != null){
            // TODO support selection by driver name
        } else {
            throw new ConfigurationException(null, "Could not determine driver to use. Specify either " 
                + DataSourceFactory.OSGI_JDBC_DRIVER_CLASS + " or " + DataSourceFactory.OSGI_JDBC_DRIVER_NAME);
        }
        
        try {
            DataSource ds = dsf.createDataSource(toProperties(config));
            ServiceRegistration reg = context.registerService(DataSource.class.getName(), ds, config);
            serviceRegs.put(pid, reg);
        } catch (SQLException e) {
            throw new ConfigurationException(null, "Error when creating the DataSource. " + e.getMessage(), e);
        }
    }



    @Override
    public void deleted(String pid) {
        ServiceRegistration reg = serviceRegs.get(pid);
        if (reg != null) {
            reg.unregister();
        }
    }

}
