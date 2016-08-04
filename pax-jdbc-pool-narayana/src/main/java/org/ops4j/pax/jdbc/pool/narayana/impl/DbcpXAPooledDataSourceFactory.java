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
package org.ops4j.pax.jdbc.pool.narayana.impl;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.managed.DataSourceXAConnectionFactory;
import org.apache.commons.dbcp2.managed.ManagedDataSource;
import org.apache.commons.dbcp2.managed.PoolableManagedConnectionFactory;
import org.apache.commons.dbcp2.managed.TransactionRegistry;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.jboss.tm.XAResourceRecovery;
import org.ops4j.pax.jdbc.pool.common.impl.BeanConfig;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbcpXAPooledDataSourceFactory extends DbcpPooledDataSourceFactory {
    private Logger LOG = LoggerFactory.getLogger(DbcpXAPooledDataSourceFactory.class);
    protected final BundleContext bundleContext;
    protected final TransactionManager tm;

    /**
     * Initialize XA PoolingDataSourceFactory
     * 
     * @param dsFactory
     *            non pooled DataSourceFactory we delegate to
     * @param tm
     *            transaction manager (Only needed for XA mode)
     */
    public DbcpXAPooledDataSourceFactory(BundleContext bundleContext, TransactionManager tm) {
        this.bundleContext = bundleContext;
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
            final XADataSource ds = dsf.createXADataSource(getNonPoolProps(props));
            DataSourceXAConnectionFactory connFactory = new DataSourceXAConnectionFactory(tm, (XADataSource) ds);
            PoolableManagedConnectionFactory pcf = new PoolableManagedConnectionFactory(connFactory, null);
            GenericObjectPoolConfig conf = new GenericObjectPoolConfig();
            BeanConfig.configure(conf, getPoolProps(props));
            BeanConfig.configure(pcf, getPrefixed(props, FACTORY_PREFIX));
            GenericObjectPool<PoolableConnection> pool = new GenericObjectPool<PoolableConnection>(pcf, conf);
            pcf.setPool(pool);
            TransactionRegistry transactionRegistry = connFactory.getTransactionRegistry();
            final ServiceRegistration<XAResourceRecovery> registration = bundleContext.registerService(XAResourceRecovery.class, new XAResourceRecovery() {
                @Override
                public XAResource[] getXAResources() {
                    try {
                        return new XAResource[] { new Wrapper(ds.getXAConnection()) };
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, null);
            ManagedDataSource<PoolableConnection> mds = new ManagedDataSource<PoolableConnection>(pool, transactionRegistry) {
                @Override
                public void close() throws Exception {
                    registration.unregister();
                    super.close();
                }
            };
            return mds;
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
    
    class Wrapper implements XAResource {

        private final XAConnection xaConnection;
        private final XAResource xaResource;

        public Wrapper(XAConnection xaConnection) throws SQLException {
            this.xaConnection = xaConnection;
            this.xaResource = xaConnection.getXAResource();
        }

        @Override
        public void commit(Xid xid, boolean b) throws XAException {
            xaResource.commit(xid, b);
        }

        @Override
        public void end(Xid xid, int i) throws XAException {
            xaResource.end(xid, i);
        }

        @Override
        public void forget(Xid xid) throws XAException {
            xaResource.forget(xid);
        }

        @Override
        public int getTransactionTimeout() throws XAException {
            return xaResource.getTransactionTimeout();
        }

        @Override
        public boolean isSameRM(XAResource xaResource) throws XAException {
            return this.xaResource.isSameRM(xaResource);
        }

        @Override
        public int prepare(Xid xid) throws XAException {
            return xaResource.prepare(xid);
        }

        @Override
        public Xid[] recover(int i) throws XAException {
            if (i == TMENDRSCAN) {
                try {
                    xaConnection.close();
                    return null;
                } catch (SQLException e) {
                    throw (XAException) new XAException(XAException.XAER_RMERR).initCause(e);
                }
            } else {
                return xaResource.recover(i);
            }
        }

        @Override
        public void rollback(Xid xid) throws XAException {
            xaResource.rollback(xid);
        }

        @Override
        public boolean setTransactionTimeout(int i) throws XAException {
            return xaResource.setTransactionTimeout(i);
        }

        @Override
        public void start(Xid xid, int i) throws XAException {
            xaResource.start(xid, i);
        }
    }
}
