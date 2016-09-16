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
package org.ops4j.pax.jdbc.pool.hikaricp.impl;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.ops4j.pax.jdbc.pool.common.PooledDataSourceFactory;
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
public class HikariPooledDataSourceFactory implements PooledDataSourceFactory {
    private static final Logger LOG = LoggerFactory.getLogger(HikariPooledDataSourceFactory.class);
    protected static final String POOL_PREFIX = "hikari.";

    public HikariPooledDataSourceFactory() {
    }

    protected Properties getNonPoolProps(Properties props) {
        Properties dsProps = new Properties();
        for (Object keyO : props.keySet()) {
            String key = (String) keyO;
            if (!key.startsWith(POOL_PREFIX)) {
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
    public DataSource create(DataSourceFactory dsf, Properties config) throws SQLException {
        try {
            DataSource ds = dsf.createDataSource(getNonPoolProps(config));
            Properties poolProps = getPoolProps(config);
            HikariConfig hconfig = new HikariConfig(poolProps);
            hconfig.setDataSource(ds);
            return new HikariDataSource(hconfig);
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
}
