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
package org.ops4j.pax.jdbc.pool.hikaricp.impl.ds;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.osgi.service.jdbc.DataSourceFactory;

public class HikariXAPooledDataSourceFactory extends HikariPooledDataSourceFactory {

    /**
     * Initialize XA PoolingDataSourceFactory
     *
     * @param dsFactory
     *            non pooled DataSourceFactory we delegate to
     * @param tm
     *            transaction manager (Only needed for XA mode)
     */
    public HikariXAPooledDataSourceFactory(DataSourceFactory dsFactory, TransactionManager tm) {
        super(dsFactory);

    }

    /**
     * HikariCp doesn't support XA datasources this method is not supported.
     * 
     * @param props
     *            properties
     */
    @Override
    public DataSource createDataSource(Properties props) throws SQLException {
        throw new SQLException("Not supported");
    }
}
