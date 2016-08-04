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
package org.ops4j.pax.jdbc.pool.dbcp2.impl;

import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;

import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.managed.DataSourceXAConnectionFactory;
import org.apache.commons.dbcp2.managed.ManagedDataSource;
import org.apache.commons.dbcp2.managed.PoolableManagedConnectionFactory;
import org.apache.commons.dbcp2.managed.TransactionRegistry;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.ops4j.pax.jdbc.pool.common.impl.BeanConfig;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbcpXAPooledDataSourceFactory extends DbcpPooledDataSourceFactory {
    private Logger LOG = LoggerFactory.getLogger(DbcpXAPooledDataSourceFactory.class);
    protected TransactionManager tm;

    /**
     * Initialize XA PoolingDataSourceFactory
     * 
     * @param dsFactory
     *            non pooled DataSourceFactory we delegate to
     * @param tm
     *            transaction manager (Only needed for XA mode)
     */
    public DbcpXAPooledDataSourceFactory(TransactionManager tm) {
        this.tm = tm;

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
    public DataSource create(DataSourceFactory dsf, Properties props) throws SQLException {
        try {
            XADataSource ds = dsf.createXADataSource(getNonPoolProps(props));
            DataSourceXAConnectionFactory connFactory = new DataSourceXAConnectionFactory(tm, (XADataSource) ds);
            PoolableManagedConnectionFactory pcf = new PoolableManagedConnectionFactory(connFactory, null);
            GenericObjectPoolConfig conf = new GenericObjectPoolConfig();
            BeanConfig.configure(conf, getPoolProps(props));
            BeanConfig.configure(pcf, getPrefixed(props, FACTORY_PREFIX));
            GenericObjectPool<PoolableConnection> pool = new GenericObjectPool<PoolableConnection>(pcf, conf);
            pcf.setPool(pool);
            TransactionRegistry transactionRegistry = connFactory.getTransactionRegistry();
            return new ManagedDataSource<PoolableConnection>(pool, transactionRegistry);
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
}
