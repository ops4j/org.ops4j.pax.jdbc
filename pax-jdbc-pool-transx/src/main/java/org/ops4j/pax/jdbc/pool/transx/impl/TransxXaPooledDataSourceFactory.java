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

import org.ops4j.pax.transx.jdbc.ManagedDataSourceBuilder;
import org.ops4j.pax.transx.tm.TransactionManager;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.resource.spi.TransactionSupport.TransactionSupportLevel;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Creates pooled and optionally XA ready DataSources out of a non pooled DataSourceFactory.
 * 
 * XA transaction handling Besides pooling this also supports to provide a DataSource that wraps a
 * XADataSource and handles the XA Resources. This kind of DataSource can then for example be used
 * in persistence.xml as jta-data-source
 */
public class TransxXaPooledDataSourceFactory extends TransxPooledDataSourceFactory {
    private  static final Logger LOG = LoggerFactory.getLogger(TransxXaPooledDataSourceFactory.class);
    private TransactionManager tm;

    /**
     * Initialize XA PooledDataSourceFactory
     * 
     * @param tm
     *            transaction manager (Only needed for XA mode)
     */
    public TransxXaPooledDataSourceFactory(TransactionManager tm) {
        this.tm = tm;
    }

    @Override
    public DataSource create(DataSourceFactory dsf, Properties props) throws SQLException {
        try {
            String name=props.getProperty(DS_USERNAME);
            XADataSource ds = dsf.createXADataSource(getNonPoolProps(props));
            DataSource mds = ManagedDataSourceBuilder.builder().name(name)
                    .dataSource(ds)
                    .transaction(TransactionSupportLevel.XATransaction)
                    .transactionManager(tm)
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

}
