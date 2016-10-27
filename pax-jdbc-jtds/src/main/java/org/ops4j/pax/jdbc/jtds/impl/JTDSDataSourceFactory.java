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
package org.ops4j.pax.jdbc.jtds.impl;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.osgi.service.jdbc.DataSourceFactory;

public class JTDSDataSourceFactory implements DataSourceFactory {

    private static final String JTDS_DRIVER_FQCN = "net.sourceforge.jtds.jdbc.Driver";
    private static final String JTDS_DATASOURCE_FQCN = "net.sourceforge.jtds.jdbcx.JtdsDataSource";

    private final Class<?> jtdsDriverClass;
    private final Class<?> jtdsDataSourceClass;

    public JTDSDataSourceFactory() throws ClassNotFoundException {
        super();
        ClassLoader classLoader = this.getClass().getClassLoader();
        this.jtdsDriverClass = classLoader.loadClass(JTDS_DRIVER_FQCN);
        this.jtdsDataSourceClass = classLoader.loadClass(JTDS_DATASOURCE_FQCN);
    }

    @Override
    public DataSource createDataSource(Properties props) throws SQLException {
        try {
            return setProperties(jtdsDataSourceClass.newInstance(), props);
        }
        catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource(Properties props)
        throws SQLException {
        try {
            return setProperties(jtdsDataSourceClass.newInstance(), props);
        }
        catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public XADataSource createXADataSource(Properties props) throws SQLException {
        try {
            return setProperties(jtdsDataSourceClass.newInstance(), props);
        }
        catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public Driver createDriver(Properties props) throws SQLException {
        try {
            return Driver.class.cast(jtdsDriverClass.newInstance());
        }
        catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T setProperties(Object dataSourceInstance, Properties props) throws Exception {
        setProperty(props.getProperty(DataSourceFactory.JDBC_DATABASE_NAME), dataSourceInstance,
            "setDatabaseName");
        setProperty(props.getProperty(DataSourceFactory.JDBC_SERVER_NAME), dataSourceInstance,
            "setServerName");
        setIntProperty(props.getProperty(DataSourceFactory.JDBC_PORT_NUMBER), dataSourceInstance,
            "setPortNumber");
        setProperty(props.getProperty(DataSourceFactory.JDBC_USER), dataSourceInstance, "setUser");
        setProperty(props.getProperty(DataSourceFactory.JDBC_PASSWORD), dataSourceInstance,
            "setPassword");
        return (T) dataSourceInstance;
    }

    private void setProperty(String value, Object instance, String methodName) throws Exception {
        if (value != null) {
            instance.getClass().getMethod(methodName, String.class).invoke(instance, value);
        }
    }

    private void setIntProperty(String value, Object instance, String methodName) throws Exception {
        if (value != null) {
            int iValue = new Integer(value);
            instance.getClass().getMethod(methodName, int.class).invoke(instance, iValue);
        }
    }

}
