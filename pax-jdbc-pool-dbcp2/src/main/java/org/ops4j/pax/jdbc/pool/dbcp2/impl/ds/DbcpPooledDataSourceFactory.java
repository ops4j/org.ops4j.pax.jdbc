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
import org.apache.commons.dbcp2.DataSourceConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.ops4j.pax.jdbc.pool.common.impl.ds.PooledDataSourceFactory;
import org.osgi.service.jdbc.DataSourceFactory;

/**
 * Creates pooled and optionally XA ready DataSources out of a non pooled DataSourceFactory. XA transaction
 * handling Besides pooling this also supports to provide a DataSource that wraps a XADataSource and handles
 * the XA Resources. This kind of DataSource can then for example be used in persistence.xml as
 * jta-data-source
 */
public class DbcpPooledDataSourceFactory extends PooledDataSourceFactory {
    /**
     * Initialize non XA PoolingDataSourceFactory
     * 
     * @param dsFactory non pooled DataSourceFactory we delegate to
     */
    public DbcpPooledDataSourceFactory(DataSourceFactory dsFactory) {
      super(dsFactory);
    }
    protected Map<String, String> getPoolProps(Properties props) {
        Map<String, String> poolProps = super.getPoolProps(props);
        if (poolProps.get("jmxNameBase") == null) {
            poolProps.put("jmxNameBase", "org.ops4j.pax.jdbc.pool.dbcp2:type=GenericObjectPool,name=");
        }
        String dsName = (String)props.get(DataSourceFactory.JDBC_DATASOURCE_NAME);
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
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("Invalid object name for data source" + dsName, e);
        }
    }
    @Override
    protected DataSource doStart(Iterable<Object> mds) throws Exception {
      Iterator<Object> it = mds.iterator();
      GenericObjectPoolConfig conf = (GenericObjectPoolConfig) it.next();
      PoolableConnectionFactory pcf = (PoolableConnectionFactory) it.next();
      GenericObjectPool<PoolableConnection> pool = new GenericObjectPool<PoolableConnection>(pcf, conf);
      return new CloseablePoolingDataSource<PoolableConnection>(pool);
     
    }
    @Override
    protected Iterable<Object> internalCreateDatasource(Object ds) {
      DataSourceConnectionFactory connFactory = new DataSourceConnectionFactory((DataSource) ds);
      PoolableConnectionFactory pcf = new PoolableConnectionFactory(connFactory, null);
      GenericObjectPoolConfig conf = new GenericObjectPoolConfig();
      Collection<Object> ret = new LinkedHashSet<Object>();
      ret.add(conf);
      ret.add(pcf);
      return ret;
    }
}
