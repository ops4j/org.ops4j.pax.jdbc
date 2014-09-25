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
package org.ops4j.pax.jdbc.pool.impl;

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
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
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
    private static final String POOL_USE_XA = "pool.usexa";
    private Logger LOG = LoggerFactory.getLogger(PooledDataSourceFactory.class);
    private DataSourceFactory dsFactory;
    private BundleContext context;

    /**
     * 
     * @param dsFactory non pooled DataSourceFactory we delegate to
     * @param tm transaction manager (Only needed for XA mode)
     */
    public PooledDataSourceFactory(DataSourceFactory dsFactory, BundleContext context) {
        this.dsFactory = dsFactory;
        this.context = context;
    }

    @Override
    public DataSource createDataSource(Properties props) throws SQLException {
        try {
            boolean useXA = Boolean.valueOf(props.getProperty(POOL_USE_XA, "true"));
            Properties dsProps = getNonPoolProps(props);
            props.remove(POOL_USE_XA);
            ConnectionFactory cf = useXA ? createXAConnectionFactory(dsProps) : createConnectionFactory(dsProps);
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
    

    private DataSourceConnectionFactory createConnectionFactory(Properties props) throws SQLException {
        DataSource ds = dsFactory.createDataSource(props);
        DataSourceConnectionFactory cf = new DataSourceConnectionFactory(ds);
        return cf;
    }
    
    /**
     * TODO How to handle the case if TransactionManager is not yet present because of startup order
     * @return TransactionManager from service or null if non is present
     */
    private TransactionManager getTransactionManager() {
        ServiceReference<TransactionManager> ref =  context.getServiceReference(TransactionManager.class);
        return ref==null ? null : context.getService(ref);
    }
    
    private DataSourceXAConnectionFactory createXAConnectionFactory(Properties props) throws SQLException {
        TransactionManager tm = getTransactionManager();
        XADataSource ds = dsFactory.createXADataSource(props);
        return new DataSourceXAConnectionFactory(tm, ds);
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
