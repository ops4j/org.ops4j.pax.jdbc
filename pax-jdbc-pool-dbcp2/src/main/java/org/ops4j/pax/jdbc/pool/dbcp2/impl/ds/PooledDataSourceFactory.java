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
package org.ops4j.pax.jdbc.pool.dbcp2.impl.ds;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DataSourceConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.managed.DataSourceXAConnectionFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates pooled and optionally XA ready DataSources out of a non pooled DataSourceFactory.
 * 
 * XA transaction handling
 * Besides pooling this also supports to provide a DataSource that wraps a XADataSource and
 * handles the XA Resources. This kind of DataSource can then for example be used in persistence.xml
 * as jta-data-source
 */
public class PooledDataSourceFactory implements DataSourceFactory {
    private static final String POOL_PREFIX = "pool.";
    private Logger LOG = LoggerFactory.getLogger(PooledDataSourceFactory.class);
    private DataSourceFactory dsFactory;
    private TransactionManager tm;

    /**
     * Initialize XA PoolingDataSourceFactory
     * @param dsFactory non pooled DataSourceFactory we delegate to
     * @param tm transaction manager (Only needed for XA mode)
     */
    public PooledDataSourceFactory(DataSourceFactory dsFactory, TransactionManager tm) {
        this.dsFactory = dsFactory;
        this.tm = tm;
    }
    
    /**
     * Initialize non XA PoolingDataSourceFactory
     * @param dsFactory non pooled DataSourceFactory we delegate to
     */
    public PooledDataSourceFactory(DataSourceFactory dsFactory) {
        this(dsFactory, null);
    }

    @Override
    public DataSource createDataSource(Properties props) throws SQLException {
        try {
            Properties dsProps = getNonPoolProps(props);
            ConnectionFactory cf = createConnectionFactory(dsProps);
            PoolableConnectionFactory pcf = new PoolableConnectionFactory(cf, null);
            GenericObjectPool<PoolableConnection> pool = new GenericObjectPool<PoolableConnection>(pcf);
            Map<String, String> poolProps = getPoolProps(props);
            GenericObjectPoolConfig conf = new GenericObjectPoolConfig();
            BeanConfig.configure(conf, poolProps);
            pool.setConfig(conf);
            return new CloseablePoolingDataSource<PoolableConnection>(pool);
        }  catch (Throwable e) {
           LOG.error("Error creating pooled datasource" + e.getMessage(), e);
           if (e instanceof SQLException) {
               throw (SQLException)e;
           } else if (e instanceof RuntimeException){
               throw (RuntimeException)e;
           } else {
               throw new RuntimeException(e.getMessage(), e);
           }
        }
    }

    private Map<String, String> getPoolProps(Properties props) {
        Map<String, String> poolProps = new HashMap<String, String>();
        for (Object keyO : props.keySet()) {
            String key = (String)keyO;
            if (key.startsWith(POOL_PREFIX)) {
                String strippedKey = key.substring(POOL_PREFIX.length());
                poolProps.put(strippedKey, (String)props.get(key));
            }
        }
        return poolProps;
    }

    private Properties getNonPoolProps(Properties props) {
        Properties dsProps = new Properties();
        for (Object keyO : props.keySet()) {
            String key = (String)keyO;
            if (!key.startsWith(POOL_PREFIX)) {
                dsProps.put(key, props.get(key));
            }
        }
        return dsProps;
    }
    

    private ConnectionFactory createConnectionFactory(Properties props) throws SQLException {
        if (tm != null) {
            XADataSource ds = dsFactory.createXADataSource(props);
            return new DataSourceXAConnectionFactory(tm, ds);
        } else {
            DataSource ds = dsFactory.createDataSource(props);
            return new DataSourceConnectionFactory(ds);
        }
    }
    
    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource(Properties props) throws SQLException {
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
