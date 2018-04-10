package org.ops4j.pax.jdbc.pool.common;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.osgi.service.jdbc.DataSourceFactory;

/**
 * PAX-JDBC specific extension to standard OSGi/JDBC concept of {@link DataSourceFactory}.
 */
public interface PooledDataSourceFactory {

    /**
     * A logical name (key) of registered {@code PooledDataSourceFactory}
     */
    String POOL_KEY = "pool";

    /**
     * A boolean flag indicating whether the registered {@code PooledDataSourceFactory} is or is not XA-Aware.
     */
    String XA_KEY = "xa";

    /**
     * Method similar to {@link DataSourceFactory} factory methods.
     * It creates pooled {@link DataSource} using OSGi JDBC standard {@link DataSourceFactory}.
     * @param dsf existing {@link DataSourceFactory} that can be used to create {@link DataSource}, {@link javax.sql.XADataSource}
     * or {@link javax.sql.ConnectionPoolDataSource} depending on configuration properties
     * @param config pooling and connection factory configuration
     * @return poolable {@link DataSource}
     * @throws SQLException
     */
    DataSource create(DataSourceFactory dsf, Properties config) throws SQLException;

}
