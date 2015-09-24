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
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;

import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.DataSources;

public class C3p0XAPooledDataSourceFactory extends C3p0PooledDataSourceFactory {
    private Logger LOG = LoggerFactory.getLogger(C3p0XAPooledDataSourceFactory.class);
    protected TransactionManager tm;

    /**
     * Initialize XA PoolingDataSourceFactory
     * 
     * @param dsFactory
     *            non pooled DataSourceFactory we delegate to
     * @param tm
     *            transaction manager (Only needed for XA mode)
     */
    public C3p0XAPooledDataSourceFactory(DataSourceFactory dsFactory, TransactionManager tm) {
        super(dsFactory);
        this.tm = tm;

    }

    @Override
    public DataSource createDataSource(Properties props) throws SQLException {
        try {
            final XADataSource unpooledDataSource = dsFactory.createXADataSource(getNonPoolProps(props));
            return DataSources.pooledDataSource((DataSource) unpooledDataSource, props);
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
