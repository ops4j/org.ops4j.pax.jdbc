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

import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;

import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.managed.DataSourceXAConnectionFactory;
import org.apache.commons.dbcp2.managed.PoolableManagedConnectionFactory;
import org.apache.commons.dbcp2.managed.TransactionRegistry;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jdbc.DataSourceFactory;

public class XAPooledDataSourceFactory extends PooledDataSourceFactory {
    private TransactionManager tm;

    /**
     * Initialize XA PoolingDataSourceFactory
     * 
     * @param dsFactory
     *            non pooled DataSourceFactory we delegate to
     * @param tm
     *            transaction manager (Only needed for XA mode)
     */
    public XAPooledDataSourceFactory(DataSourceFactory dsFactory, TransactionManager tm) {
        super(dsFactory);
        this.tm = tm;
    }
    
    @Override
    protected DataSource createDataSourceInternal(Properties props, Map<String, String> poolProps) throws SQLException {
        XADataSource ds = dsFactory.createXADataSource(props);
        DataSourceXAConnectionFactory connFactory = new DataSourceXAConnectionFactory(tm, ds);
        PoolableManagedConnectionFactory pcf = new PoolableManagedConnectionFactory(connFactory, null);
        GenericObjectPoolConfig conf = new GenericObjectPoolConfig();
        BeanConfig.configure(conf, poolProps);
        GenericObjectPool<PoolableConnection> pool = new GenericObjectPool<PoolableConnection>(pcf, conf);
        TransactionRegistry transactionRegistry = connFactory.getTransactionRegistry();
        return new CloseableManagedDataSource<PoolableConnection>(pool, transactionRegistry);
    }

    @Override
    public Dictionary<String, Object> createPropsForPoolingDataSourceFactory(ServiceReference<DataSourceFactory> reference) {
        Dictionary<String, Object> props = super.createPropsForPoolingDataSourceFactory(reference);
        props.put("xa", "true");
        return props;
    }

    @Override
    protected String getPoolDriverName(ServiceReference<DataSourceFactory> reference) {
        return super.getPoolDriverName(reference) + "-xa";
    }

}
