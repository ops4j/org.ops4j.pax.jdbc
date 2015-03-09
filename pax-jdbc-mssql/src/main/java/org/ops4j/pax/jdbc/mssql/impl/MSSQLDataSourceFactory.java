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
package org.ops4j.pax.jdbc.mssql.impl;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.osgi.service.jdbc.DataSourceFactory;

public class MSSQLDataSourceFactory implements DataSourceFactory {

    private static final String MSSQL_DRIVER_FQCN = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String MSSQL_DATASOURCE_FQCN = "com.microsoft.sqlserver.jdbc.SQLServerDataSource";
    private static final String MSSQL_CONNECTIONPOOL_DATASOURCE_FQCN = "com.microsoft.sqlserver.jdbc.SQLServerConnectionPoolDataSource";
    private static final String MSSQL_XA_DATASOURCE_FQCN = "com.microsoft.sqlserver.jdbc.SQLServerXADataSource";

    private final Class<?> mssqlDriverClass;
    private final Class<?> mssqlDataSourceClass;
    private final Class<?> mssqlConnectionPoolDataSourceClass;
    private final Class<?> mssqlXADataSourceClass;

    public MSSQLDataSourceFactory() throws ClassNotFoundException {
        super();
        ClassLoader classLoader = this.getClass().getClassLoader();
        this.mssqlDriverClass = classLoader.loadClass(MSSQL_DRIVER_FQCN);
        this.mssqlDataSourceClass = classLoader.loadClass(MSSQL_DATASOURCE_FQCN);
        this.mssqlConnectionPoolDataSourceClass = classLoader
            .loadClass(MSSQL_CONNECTIONPOOL_DATASOURCE_FQCN);
        this.mssqlXADataSourceClass = classLoader.loadClass(MSSQL_XA_DATASOURCE_FQCN);
    }

    @Override
    public DataSource createDataSource(Properties props) throws SQLException {
        try {
            DataSource ds = DataSource.class.cast(mssqlDataSourceClass.newInstance());
            setProperties(mssqlDataSourceClass, ds, props);
            return ds;
        }
        catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource(Properties props)
        throws SQLException {
        try {
            ConnectionPoolDataSource cpds = ConnectionPoolDataSource.class
                .cast(mssqlConnectionPoolDataSourceClass.newInstance());
            setProperties(mssqlConnectionPoolDataSourceClass, cpds, props);
            return cpds;
        }
        catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public XADataSource createXADataSource(Properties props) throws SQLException {
        try {
            XADataSource xads = XADataSource.class.cast(mssqlXADataSourceClass.newInstance());
            setProperties(mssqlXADataSourceClass, xads, props);
            return xads;
        }
        catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public Driver createDriver(Properties props) throws SQLException {
        try {
            return Driver.class.cast(mssqlDriverClass.newInstance());
        }
        catch (InstantiationException ex) {
            throw new SQLException(ex);
        }
        catch (IllegalAccessException ex) {
            throw new SQLException(ex);
        }
    }

    private void setProperties(Class<?> dataSourceClass, Object dataSourceInstance, Properties props)
        throws Exception {

        setProperty(props.getProperty(DataSourceFactory.JDBC_URL), dataSourceClass,
            dataSourceInstance, "setURL");
        setProperty(props.getProperty(DataSourceFactory.JDBC_DATABASE_NAME), dataSourceClass,
            dataSourceInstance, "setDatabaseName");
        setProperty(props.getProperty(DataSourceFactory.JDBC_SERVER_NAME), dataSourceClass,
            dataSourceInstance, "setServerName");
        setProperty(props.getProperty(DataSourceFactory.JDBC_PORT_NUMBER), dataSourceClass,
            dataSourceInstance, "setPortNumber");
        setProperty(props.getProperty(DataSourceFactory.JDBC_USER), dataSourceClass,
            dataSourceInstance, "setUser");
        setProperty(props.getProperty(DataSourceFactory.JDBC_PASSWORD), dataSourceClass,
            dataSourceInstance, "setPassword");

    }

    private void setProperty(String propertyValue, Class<?> dataSourceClass,
        Object dataSourceInstance, String dataSourceMethodName) throws Exception {
        if (propertyValue != null) {
            dataSourceClass.getMethod(dataSourceMethodName, String.class).invoke(
                dataSourceInstance, propertyValue);
        }
    }

}
