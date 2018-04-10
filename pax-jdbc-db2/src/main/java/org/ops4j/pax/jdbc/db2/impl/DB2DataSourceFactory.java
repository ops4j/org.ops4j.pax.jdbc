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
package org.ops4j.pax.jdbc.db2.impl;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.CommonDataSource;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.osgi.service.jdbc.DataSourceFactory;

public class DB2DataSourceFactory implements DataSourceFactory {

    public static final String DB2_DRIVER_CLASS = "com.ibm.db2.jcc.DB2Driver";
    private static final String DB2_DATASOURCE_CLASS = "com.ibm.db2.jcc.DB2SimpleDataSource";
    private static final String DB2_CONNECTIONPOOL_DATASOURCE_CLASS = "com.ibm.db2.jcc.DB2ConnectionPoolDataSource";
    private static final String DB2_XA_DATASOURCE_CLASS = "com.ibm.db2.jcc.DB2XADataSource";
    private final Class<? extends DataSource> db2DataSourceClass;
    private final Class<? extends ConnectionPoolDataSource> db2ConnectionPoolDataSourceClass;
    private final Class<? extends XADataSource> db2XaDataSourceClass;
    private final Class<?> db2DriverClass;

    @SuppressWarnings("unchecked")
    public DB2DataSourceFactory() throws ClassNotFoundException {
        ClassLoader classLoader = DB2DataSourceFactory.class.getClassLoader();
        this.db2DataSourceClass = (Class<? extends DataSource>)classLoader.loadClass(DB2_DATASOURCE_CLASS);
        this.db2ConnectionPoolDataSourceClass = (Class<? extends ConnectionPoolDataSource>)classLoader.loadClass(DB2_CONNECTIONPOOL_DATASOURCE_CLASS);
        this.db2XaDataSourceClass = (Class<? extends XADataSource>)classLoader.loadClass(DB2_XA_DATASOURCE_CLASS);
        this.db2DriverClass = classLoader.loadClass(DB2_DRIVER_CLASS);
    }

    @Override
    public DataSource createDataSource(Properties props) throws SQLException {
        return create(db2DataSourceClass, props);
    }

    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource(Properties props)
            throws SQLException {
        return create(db2ConnectionPoolDataSourceClass, props);
    }

    @Override
    public XADataSource createXADataSource(Properties props) throws SQLException {
        return create(db2XaDataSourceClass, props);
    }

    private static <T extends CommonDataSource> T create(Class<T> target, Properties props) throws SQLException {
        try {
            T ds = target.cast(target.newInstance());
            setProperties(ds, target, props);
            return ds;
        }
        catch (Exception ex) {
            throw new SQLException(ex);
        }

    }

    private static void setProperties(CommonDataSource ds, Class<?> clazz, Properties properties)
            throws Exception {
        final Map<String, Object> map = new HashMap<>();
        for (Map.Entry<Object, Object> e: properties.entrySet()) {
            map.put(e.getKey().toString(), e.getValue());
        }
        BeanConfig.configure(ds, new HashMap<String, Object>(map));
    }

    @Override
    public Driver createDriver(Properties props) throws SQLException {
        try {
            return Driver.class.cast(db2DriverClass.newInstance());
        }
        catch (InstantiationException ex) {
            throw new SQLException(ex);
        }
        catch (IllegalAccessException ex) {
            throw new SQLException(ex);
        }
    }
}