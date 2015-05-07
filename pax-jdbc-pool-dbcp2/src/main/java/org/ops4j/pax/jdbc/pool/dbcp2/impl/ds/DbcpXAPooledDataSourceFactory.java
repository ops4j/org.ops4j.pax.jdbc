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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.managed.DataSourceXAConnectionFactory;
import org.apache.commons.dbcp2.managed.PoolableManagedConnectionFactory;
import org.apache.commons.dbcp2.managed.TransactionRegistry;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.ops4j.pax.jdbc.pool.common.impl.ds.XAPooledDataSourceFactory;
import org.osgi.service.jdbc.DataSourceFactory;

public class DbcpXAPooledDataSourceFactory extends XAPooledDataSourceFactory {

    /**
     * Initialize XA PoolingDataSourceFactory
     * 
     * @param dsFactory
     *            non pooled DataSourceFactory we delegate to
     * @param tm
     *            transaction manager (Only needed for XA mode)
     */
    public DbcpXAPooledDataSourceFactory(DataSourceFactory dsFactory, TransactionManager tm) {
        super(dsFactory, tm);

    }

    protected Map<String, String> getPoolProps(Properties props) {
        Map<String, String> poolProps = super.getPoolProps(props);
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
        Properties dsProps = super.getNonPoolProps(props);
        dsProps.remove(DataSourceFactory.JDBC_DATASOURCE_NAME);
        return dsProps;
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
    protected Iterable<Object> internalCreateDatasource(Object ds) {
        DataSourceXAConnectionFactory connFactory = new DataSourceXAConnectionFactory(tm,
            (XADataSource) ds);
        PoolableManagedConnectionFactory pcf = new PoolableManagedConnectionFactory(connFactory,
            null);
        GenericObjectPoolConfig conf = new GenericObjectPoolConfig();
        Collection<Object> ret = new LinkedHashSet<Object>();
        ret.add(conf);
        ret.add(pcf);
        ret.add(connFactory);
        return ret;
    }

    @Override
    protected DataSource doStart(Iterable<Object> mds) throws Exception {
        Iterator<Object> it = mds.iterator();
        GenericObjectPoolConfig conf = (GenericObjectPoolConfig) it.next();
        PoolableManagedConnectionFactory pcf = (PoolableManagedConnectionFactory) it.next();
        DataSourceXAConnectionFactory connFactory = (DataSourceXAConnectionFactory) it.next();
        GenericObjectPool<PoolableConnection> pool = new GenericObjectPool<PoolableConnection>(pcf,
            conf);
        TransactionRegistry transactionRegistry = connFactory.getTransactionRegistry();
        return new CloseableManagedDataSource<PoolableConnection>(pool, transactionRegistry);

    }

}
