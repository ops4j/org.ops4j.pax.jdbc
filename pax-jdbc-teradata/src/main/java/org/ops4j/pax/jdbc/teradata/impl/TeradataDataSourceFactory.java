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
package org.ops4j.pax.jdbc.teradata.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.ops4j.pax.jdbc.common.BeanConfig;
import org.osgi.service.jdbc.DataSourceFactory;

public class TeradataDataSourceFactory implements DataSourceFactory {

    private static final String TERA_DATASOURCE_CLASS = "com.teradata.jdbc.TeraDataSource";
    private static final String TERA_CONNECTIONPOOL_DATASOURCE_CLASS = "com.teradata.jdbc.TeraConnectionPoolDataSource";

    private static final String TERA_DRIVER_CLASS = "com.teradata.jdbc.TeraDriver";

    private final Class<?> teraDataSourceClass;
    private final Class<?> teraConnectionPoolDataSourceClass;

    private final Class<?> teraDriverClass;

    public TeradataDataSourceFactory() throws ClassNotFoundException {
        ClassLoader classLoader = TeradataDataSourceFactory.class.getClassLoader();
        this.teraDataSourceClass = classLoader.loadClass(TERA_DATASOURCE_CLASS);
        this.teraConnectionPoolDataSourceClass = classLoader.loadClass(TERA_CONNECTIONPOOL_DATASOURCE_CLASS);
        this.teraDriverClass = classLoader.loadClass(TERA_DRIVER_CLASS);
    }

    @Override
    public DataSource createDataSource(Properties props) throws SQLException {
        try {
            return setProperties(teraDataSourceClass.newInstance(), props);
        } catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource(
            Properties props) throws SQLException {
        try {
            return setProperties(teraConnectionPoolDataSourceClass.newInstance(), props);
        } catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public XADataSource createXADataSource(Properties props)
            throws SQLException {

        throw new SQLException("XADataSource not supported");

    }

    @Override
    public Driver createDriver(Properties props) throws SQLException {
        try {
            return Driver.class.cast(teraDriverClass.newInstance());
        } catch (InstantiationException ex) {
            throw new SQLException(ex);
        } catch (IllegalAccessException ex) {
            throw new SQLException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T setProperties(Object dataSourceInstance, Properties properties) throws Exception {
        Properties props = (Properties) properties.clone();

        String url = (String) props.remove(DataSourceFactory.JDBC_URL);
        if (url != null) {
            parseUrlAndSetProperty(url.substring(5), dataSourceInstance);
        }

        String databaseName = (String) props.remove(DataSourceFactory.JDBC_DATABASE_NAME);
        if (databaseName == null && url == null) {
            throw new SQLException("missing required property " + DataSourceFactory.JDBC_DATABASE_NAME);
        }

        if (databaseName != null) {
            setProperty(databaseName, dataSourceInstance, "setDatabaseName");
        }

        String serverName = (String) props.remove(DataSourceFactory.JDBC_SERVER_NAME);
        if (serverName != null) {
            setProperty(serverName, dataSourceInstance, "setDSName");
        }

        String portNumber = (String) props.remove(DataSourceFactory.JDBC_PORT_NUMBER);
        if (portNumber != null) {
            setIntProperty(portNumber, dataSourceInstance, "setDbsPort");
        }

        String user = (String) props.remove(DataSourceFactory.JDBC_USER);
        if (user != null) {
            setProperty(user, dataSourceInstance, "setUser");
        }

        String password = (String) props.remove(DataSourceFactory.JDBC_PASSWORD);
        if (password != null) {
            setProperty(password, dataSourceInstance, "setPassword");
        }

        if (!props.isEmpty()) {
            BeanConfig.configure(dataSourceInstance, props);
        }
        return (T) dataSourceInstance;
    }

    private void setProperty(String value, Object instance, String methodName) throws Exception {
        if (value != null) {
            instance.getClass().getMethod(methodName, String.class).invoke(instance, value);
        }
    }

    private void setIntProperty(String value, Object instance, String methodName) throws Exception {
        int iValue = new Integer(value);
        instance.getClass().getMethod(methodName, int.class).invoke(instance, iValue);
    }

    private void parseUrlAndSetProperty(String url, Object dataSourceInstance) throws Exception {

        URI uri = URI.create(url);
        Method[] methods = dataSourceInstance.getClass().getMethods();

        if (uri.getHost() != null) {
            setProperty(uri.getHost(), dataSourceInstance, "setDSName");
        }

        if (uri.getPort() != -1) {
            setIntProperty(uri.getPort() + "", dataSourceInstance, "setDbsPort");
        }

        String path = uri.getPath().substring(1);

        String param = "";
        String value = "";

        StringTokenizer queryParams = new StringTokenizer(path, ",");

        while (queryParams.hasMoreTokens()) {

            StringTokenizer pv = new StringTokenizer(queryParams.nextToken(), "=");

            if (pv.hasMoreTokens()) {
                param = pv.nextToken();
            }

            if (pv.hasMoreTokens()) {
                value = pv.nextToken();
            }

            if (value.length() > 0 && param.length() > 0) {

                set(dataSourceInstance, param, value);
            }
        }

    }

    public static boolean set(Object object, String fieldName, Object fieldValue) {
        Class<?> clazz = object.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(object, fieldValue);
                return true;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return false;
    }

}
