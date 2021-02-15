/*
 * Copyright 2021 OPS4J.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.jdbc.db2.impl;

import java.net.URI;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

import javax.sql.CommonDataSource;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.ops4j.pax.jdbc.common.BeanConfig;
import org.osgi.service.jdbc.DataSourceFactory;

public class DB2DataSourceFactory implements DataSourceFactory {

    public static final String DB2_DRIVER_CLASS = "com.ibm.db2.jcc.DB2Driver";
    private static final String DB2_DATASOURCE_CLASS = "com.ibm.db2.jcc.DB2SimpleDataSource";
    private static final String DB2_CONNECTIONPOOL_DATASOURCE_CLASS = "com.ibm.db2.jcc.DB2ConnectionPoolDataSource";
    private static final String DB2_XA_DATASOURCE_CLASS = "com.ibm.db2.jcc.DB2XADataSource";
    private static final String DB2_PREFIX = "jdbc:db2:";
    private static final String DB2_JDBC_SERVER_NAME = "serverName";
    private static final String DB2_JDBC_DATABASE_NAME = "databaseName";
    private static final String DB2_JDBC_PORT_NUMBER = "portNumber";
    private static final String DB2_JDBC_DRIVER_TYPE = "driverType";
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

            //property 'url'  has to be handled differently
            String url = (String)props.remove(DataSourceFactory.JDBC_URL);
            if (url != null) {
                try {
                    BeanConfig.configure(ds, Collections.singletonMap(DataSourceFactory.JDBC_URL, url));
                } catch (IllegalArgumentException e) {
                    //if url can not be configured, it has to be parsed and configured by settings other properties
                    parseUrl(url, props);
                }
            }

            props.remove("url");
            BeanConfig.configure(ds, props);

            return ds;
        }
        catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    private static void parseUrl(String url, Properties props) {
        if (url == null) {
            return;
        }
        if (!url.startsWith(DB2_PREFIX)) {
            throw new IllegalArgumentException("The supplied URL is no db2 url: " + url);
        }
        URI uri = URI.create(url.substring(5));
        String suburl = uri.getPath();
        if (suburl.startsWith("/")) {
            suburl = suburl.substring(1);
        }
        String[] parts = suburl.split(";");
        String database = parts[0];

        //if path is null, it means that url for db2 of type 2, which can not be used - host and port value will be unknown
        if ("".equals(uri.getPath())) {
            throw new IllegalArgumentException("The supplied URL is no db2 (type 4) url: " + url);
        }

        props.put(DB2_JDBC_SERVER_NAME, uri.getHost());
        props.put(DB2_JDBC_DATABASE_NAME, database);
        props.put(DB2_JDBC_PORT_NUMBER, Integer.toString(uri.getPort()));
        props.put(DB2_JDBC_DRIVER_TYPE, "4");
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
