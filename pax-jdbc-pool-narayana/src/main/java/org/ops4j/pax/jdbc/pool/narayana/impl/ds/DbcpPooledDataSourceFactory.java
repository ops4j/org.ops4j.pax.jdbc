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
package org.ops4j.pax.jdbc.pool.narayana.impl.ds;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.dbcp2.DataSourceConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.ops4j.pax.jdbc.pool.common.impl.BeanConfig;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates pooled and optionally XA ready DataSources out of a non pooled DataSourceFactory. XA
 * transaction handling Besides pooling this also supports to provide a DataSource that wraps a
 * XADataSource and handles the XA Resources. This kind of DataSource can then for example be used
 * in persistence.xml as jta-data-source
 */
public class DbcpPooledDataSourceFactory implements DataSourceFactory {
    private static final Logger LOG = LoggerFactory.getLogger(DbcpPooledDataSourceFactory.class);
    protected static final String POOL_PREFIX = "pool.";
    protected static final String FACTORY_PREFIX = "factory.";
    protected DataSourceFactory dsFactory;

    /**
     * Initialize non XA PoolingDataSourceFactory
     * 
     * @param dsFactory
     *            non pooled DataSourceFactory we delegate to
     */
    public DbcpPooledDataSourceFactory(DataSourceFactory dsFactory) {
        this.dsFactory = dsFactory;
    }

    
    protected Map<String, String> getPoolProps(Properties props) {
        Map<String, String> poolProps = getPrefixed(props, POOL_PREFIX);
        if (poolProps.get("jmxNameBase") == null) {
            poolProps.put("jmxNameBase",
                "org.ops4j.pax.jdbc.pool.dbcp2:type=GenericObjectPool,name=");
        }
        String dsName = (String) props.get(DataSourceFactory.JDBC_DATASOURCE_NAME);
        if (dsName != null) {
            poolProps.put("jmxNamePrefix", dsName);
        }
        return poolProps;
    }

    protected Properties getNonPoolProps(Properties props) {
        Properties dsProps = new Properties();
        for (Object keyO : props.keySet()) {
            String key = (String) keyO;
            if (!key.startsWith(POOL_PREFIX) && !key.startsWith(FACTORY_PREFIX)) {
                dsProps.put(key, props.get(key));
            }
        }
        dsProps.remove(DataSourceFactory.JDBC_DATASOURCE_NAME);
        return dsProps;
    }

    protected Map<String, String> getPrefixed(Properties props, String prefix) {
        Map<String, String> prefixedProps = new HashMap<String, String>();
        for (Object keyO : props.keySet()) {
            String key = (String) keyO;
            if (key.startsWith(prefix)) {
                String strippedKey = key.substring(prefix.length());
                prefixedProps.put(strippedKey, (String) props.get(key));
            }
        }
        return prefixedProps;
    }
    
    protected ObjectName getJmxName(String dsName) {
        if (dsName == null) {
            dsName = UUID.randomUUID().toString();
        }
        try {
            return new ObjectName("org.ops4j.pax.jdbc.pool", "dsName", dsName);
        }
        catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("Invalid object name for data source" + dsName, e);
        }
    }

    @Override
    public DataSource createDataSource(Properties props) throws SQLException {
        try {
            DataSource ds = dsFactory.createDataSource(getNonPoolProps(props));
            DataSourceConnectionFactory connFactory = new DataSourceConnectionFactory((DataSource) ds);
            PoolableConnectionFactory pcf = new PoolableConnectionFactory(connFactory, null);
            GenericObjectPoolConfig conf = new GenericObjectPoolConfig();
            BeanConfig.configure(conf, getPoolProps(props));
            BeanConfig.configure(pcf, getPrefixed(props, FACTORY_PREFIX));
            GenericObjectPool<PoolableConnection> pool = new GenericObjectPool<PoolableConnection>(pcf, conf);
            pcf.setPool(pool);
            return new PoolingDataSource<PoolableConnection>(pool);
        }
        catch (Throwable e) {
            LOG.error("Error creating pooled datasource" + e.getMessage(), e);
            if (e instanceof SQLException) {
                throw (SQLException) e;
            }
            else if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            else {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }
    
    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource(Properties props)
        throws SQLException {
        throw new SQLException("Not supported");
    }

    @Override
    public XADataSource createXADataSource(Properties props) throws SQLException {
        throw new SQLException("Not supported");
    }

    @Override
    public Driver createDriver(Properties props) throws SQLException {
        throw new SQLException("Not supported");
    }
}
