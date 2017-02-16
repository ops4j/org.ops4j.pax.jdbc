/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.jdbc.oracle.impl;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.CommonDataSource;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.osgi.service.jdbc.DataSourceFactory;

public class OracleDataSourceFactory implements DataSourceFactory {

    private static final String ORACLE_DATASOURCE_CLASS = "oracle.jdbc.pool.OracleDataSource";
    private static final String ORACLE_CONNECTIONPOOL_DATASOURCE_CLASS = "oracle.jdbc.pool.OracleConnectionPoolDataSource";
    private static final String ORACLE_XA_DATASOURCE_CLASS = "oracle.jdbc.xa.client.OracleXADataSource";
    private static final String ORACLE_DRIVER_CLASS = "oracle.jdbc.OracleDriver";
    private final Class<?> oracleDataSourceClass;
    private final Class<?> oracleConnectionPoolDataSourceClass;
    private final Class<?> oracleXaDataSourceClass;
    private final Class<?> oracleDriverClass;

    public OracleDataSourceFactory() throws ClassNotFoundException {
        ClassLoader classLoader = OracleDataSourceFactory.class.getClassLoader();
        this.oracleDataSourceClass = classLoader.loadClass(ORACLE_DATASOURCE_CLASS);
        this.oracleConnectionPoolDataSourceClass = classLoader
            .loadClass(ORACLE_CONNECTIONPOOL_DATASOURCE_CLASS);
        this.oracleXaDataSourceClass = classLoader.loadClass(ORACLE_XA_DATASOURCE_CLASS);
        this.oracleDriverClass = classLoader.loadClass(ORACLE_DRIVER_CLASS);
    }

    @Override
    public DataSource createDataSource(Properties props) throws SQLException {
        try {
            DataSource ds = DataSource.class.cast(oracleDataSourceClass.newInstance());
            setProperties(ds, oracleDataSourceClass, props);
            return ds;
        }
        catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    private void setProperties(CommonDataSource ds, Class<?> clazz, Properties properties)
        throws Exception {
        Properties props = (Properties) properties.clone();

        String url = (String) props.remove(DataSourceFactory.JDBC_URL);
        if (url != null) {
            clazz.getMethod("setURL", String.class).invoke(ds, url);
        }

        String databaseName = (String) props.remove(DataSourceFactory.JDBC_DATABASE_NAME);
        if (databaseName == null && url == null) {
            throw new SQLException("missing required property "
                + DataSourceFactory.JDBC_DATABASE_NAME);
        }
        clazz.getMethod("setDatabaseName", String.class).invoke(ds, databaseName);

        String serverName = (String) props.remove(DataSourceFactory.JDBC_SERVER_NAME);
        clazz.getMethod("setServerName", String.class).invoke(ds, serverName);

        String portNumber = (String) props.remove(DataSourceFactory.JDBC_PORT_NUMBER);
        if (portNumber != null) {
            int portNum = Integer.parseInt(portNumber);
            try {
                clazz.getMethod("setPortNumber", Integer.class).invoke(ds, portNum);
            } catch (NoSuchMethodException e) {
                clazz.getMethod("setPortNumber", int.class).invoke(ds, portNum);
            }
        }

        String user = (String) props.remove(DataSourceFactory.JDBC_USER);
        clazz.getMethod("setUser", String.class).invoke(ds, user);

        String password = (String) props.remove(DataSourceFactory.JDBC_PASSWORD);
        clazz.getMethod("setPassword", String.class).invoke(ds, password);

        if (!props.isEmpty()) {
            throw new SQLException("cannot set properties " + props.keySet());
        }
    }

    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource(Properties props)
        throws SQLException {
        try {
            ConnectionPoolDataSource ds = ConnectionPoolDataSource.class
                .cast(oracleConnectionPoolDataSourceClass.newInstance());
            setProperties(ds, oracleConnectionPoolDataSourceClass, props);
            return ds;
        }
        catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public XADataSource createXADataSource(Properties props) throws SQLException {
        try {
            XADataSource ds = XADataSource.class.cast(oracleXaDataSourceClass.newInstance());
            setProperties(ds, oracleXaDataSourceClass, props);
            return ds;
        }
        catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public Driver createDriver(Properties props) throws SQLException {
        try {
            return Driver.class.cast(oracleDriverClass.newInstance());
        }
        catch (InstantiationException ex) {
            throw new SQLException(ex);
        }
        catch (IllegalAccessException ex) {
            throw new SQLException(ex);
        }
    }
}
