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
import java.sql.SQLException;
import java.util.Arrays;
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

@SuppressWarnings({
    "rawtypes", "unchecked"
})
public class DataSourceRegistration implements Closeable {

    static final String DATASOURCE_TYPE = "dataSourceType";
    private static final String JNDI_SERVICE_NAME = "osgi.jndi.service.name";

    // These config keys will not be forwarded to the DataSourceFactory
    private static final String[] NOT_FORWARDED_KEYS = {
        "service.pid", //
        DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, //
        DataSourceFactory.OSGI_JDBC_DRIVER_NAME, //
        DataSourceFactory.JDBC_DATASOURCE_NAME, //
        "service.factoryPid", //
        "felix.fileinstall.filename", //
        "aries.managed", //
        JNDI_SERVICE_NAME, //
        DATASOURCE_TYPE
    };
    private static Logger LOG = LoggerFactory.getLogger(DataSourceRegistration.class);

    private AutoCloseable dataSource;
    private ServiceRegistration serviceReg;

    public DataSourceRegistration(BundleContext context, DataSourceFactory dsf, final Dictionary config, final Dictionary decryptedConfig) {
        Object dsName = config.get(DataSourceFactory.JDBC_DATASOURCE_NAME);
        if (dsName != null && config.get(JNDI_SERVICE_NAME) == null) {
            config.put(JNDI_SERVICE_NAME, dsName);
        }
        
        try {
            String typeName = (String)config.get(DATASOURCE_TYPE);
            Class<?> type = getType(typeName);
            Object ds = createDs(dsf, type, decryptedConfig);
            if (ds instanceof AutoCloseable) {
                dataSource = (AutoCloseable)ds;
            }
            serviceReg = context.registerService(type.getName(), ds, config);
        } catch (SQLException e) {
            LOG.warn(e.getMessage(), e);
        }
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
        Set<String> ignoredKeys = new HashSet<String>(Arrays.asList(NOT_FORWARDED_KEYS));
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            if (!ignoredKeys.contains(key)) {
                props.put(key, dict.get(key));
            }
        }
        return props;
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
