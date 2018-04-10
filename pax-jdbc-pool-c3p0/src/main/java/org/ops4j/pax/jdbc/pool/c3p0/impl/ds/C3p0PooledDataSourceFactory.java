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
package org.ops4j.pax.jdbc.pool.c3p0.impl.ds;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.ops4j.pax.jdbc.pool.common.PooledDataSourceFactory;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.C3P0Registry;
import com.mchange.v2.c3p0.DataSources;
import com.mchange.v2.c3p0.PooledDataSource;

/**
 * Creates pooled and optionally XA ready DataSources out of a non pooled DataSourceFactory. XA
 * transaction handling Besides pooling this also supports to provide a DataSource that wraps a
 * XADataSource and handles the XA Resources. This kind of DataSource can then for example be used
 * in persistence.xml as jta-data-source
 */
public class C3p0PooledDataSourceFactory implements PooledDataSourceFactory {
    protected static final String POOL_PREFIX = "c3p0.";
    private static final Logger LOG = LoggerFactory.getLogger(C3p0PooledDataSourceFactory.class);

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
    
  
    
    protected final void closeDataSource(Properties props) throws SQLException {
        final String dataSourceName = props.getProperty("c3p0.dataSourceName");
        if (dataSourceName != null) {
            final PooledDataSource pds = C3P0Registry.pooledDataSourceByName(dataSourceName);
            if (pds != null) {
                LOG.info("Closing C3P0 pooled data source {}.", pds.getDataSourceName());
                pds.close();
            }
        } else {
            LOG.error("Please define c3p0.dataSourceName in your configuration to prevent memory leaks");
        }
    }

    @Override
    public DataSource create(DataSourceFactory dsf, Properties config) throws SQLException {
        try {
            closeDataSource(config);
            final DataSource unpooledDataSource = dsf.createDataSource(getNonPoolProps(config));
            return DataSources.pooledDataSource(unpooledDataSource, config);
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
