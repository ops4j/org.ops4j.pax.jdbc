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
package org.ops4j.pax.jdbc.pool.transx.impl;

import org.ops4j.pax.jdbc.pool.common.PooledDataSourceFactory;
import org.ops4j.pax.transx.jdbc.ManagedDataSourceBuilder;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.resource.spi.TransactionSupport.TransactionSupportLevel;
import javax.sql.CommonDataSource;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Creates pooled and optionally XA ready DataSources out of a non pooled DataSourceFactory.
 * 
 * XA transaction handling Besides pooling this also supports to provide a DataSource that wraps a
 * XADataSource and handles the XA Resources. This kind of DataSource can then for example be used
 * in persistence.xml as jta-data-source
 */
public class TransxPooledDataSourceFactory implements PooledDataSourceFactory {

    private  static final Logger LOG = LoggerFactory.getLogger(TransxPooledDataSourceFactory.class);
    protected static final String POOL_PREFIX = "pool.";
    protected static final String DS_USERNAME = "user";
    public DataSource create(DataSourceFactory dsf, Properties props) throws SQLException {
        try {
            String name=props.getProperty(DS_USERNAME);
            CommonDataSource ds = dsf.createDataSource(getNonPoolProps(props));
            DataSource mds = ManagedDataSourceBuilder.builder().name(name)
                    .dataSource(ds)
                    .transaction(TransactionSupportLevel.NoTransaction)
                    .build();
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
    
    protected Map<String, String> getPoolProps(Properties props) {
        Map<String, String> poolProps = new HashMap<String, String>();
        for (Object keyO : props.keySet()) {
            String key = (String) keyO;
            if (key.startsWith(POOL_PREFIX)) {
                String strippedKey = key.substring(POOL_PREFIX.length());
                poolProps.put(strippedKey, (String) props.get(key));
            }
        }
        return poolProps;
    }

    protected Properties getNonPoolProps(Properties props) {
        Properties dsProps = new Properties();
        for (Object keyO : props.keySet()) {
            String key = (String) keyO;
            if (!key.startsWith(POOL_PREFIX)) {
                dsProps.put(key, props.get(key));
            }
        }
        return dsProps;
    }

}
