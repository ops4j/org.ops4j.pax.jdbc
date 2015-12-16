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

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Creates pooled ready DataSources out of a non pooled DataSourceFactory. XA
 * datasources are not supported. This kind of DataSource can then for example be used
 * in persistence.xml as jta-data-source
 */
public class HikariPooledDataSourceFactory implements DataSourceFactory {
    private static final Logger LOG = LoggerFactory.getLogger(HikariPooledDataSourceFactory.class);
    protected static final String POOL_PREFIX = "hikari.";
    protected static final String FACTORY_PREFIX = "factory.";
    protected DataSourceFactory dsFactory;

    /**
     * Initialize non XA PoolingDataSourceFactory
     *
     * @param dsFactory
     *            non pooled DataSourceFactory we delegate to
     */
    public HikariPooledDataSourceFactory(DataSourceFactory dsFactory) {
        this.dsFactory = dsFactory;
    }

    protected Properties getNonPoolProps(Properties props) {
        Properties dsProps = new Properties();
        for (Object keyO : props.keySet()) {
            String key = (String) keyO;
            if (!key.startsWith(POOL_PREFIX) && !key.startsWith(FACTORY_PREFIX)) {
                dsProps.put(key, props.get(key));
            }
        }
        dsProps.remove(DataSourceFactory.JDBC_DATASOURCE_NAME);
        return dsProps;
    }

    protected Properties getPoolProps(Properties props) {
        Properties prefixedProps = new Properties();
        for (Object keyO : props.keySet()) {
            String key = (String) keyO;
            if (key.startsWith(POOL_PREFIX)) {
                String strippedKey = key.substring(POOL_PREFIX.length());
                prefixedProps.put(strippedKey, (String) props.get(key));
            }
        }
        return prefixedProps;
    }

    @Override
    public DataSource createDataSource(Properties props) throws SQLException {
        try {
            DataSource ds = dsFactory.createDataSource(getNonPoolProps(props));
            Properties poolProps = getPoolProps(props);
            HikariConfig config = new HikariConfig(poolProps);
            config.setDataSource(ds);
            return new HikariDataSource(config);
        } catch (Throwable e) {
            LOG.error("Error creating pooled datasource" + e.getMessage(), e);
            if (e instanceof SQLException) {
                throw (SQLException) e;
            } else if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource(Properties props)
            throws SQLException {
        throw new SQLException("Not supported");
    }

    @Override
    public XADataSource createXADataSource(Properties props) throws SQLException {
        throw new SQLException("Not supported");
    }

    @Override
    public Driver createDriver(Properties props) throws SQLException {
        throw new SQLException("Not supported");
    }
}
