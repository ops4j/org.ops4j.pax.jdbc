package org.ops4j.pax.jdbc.pool.common;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.osgi.service.jdbc.DataSourceFactory;

public interface PooledDataSourceFactory {
    public static final String POOL_KEY = "pool"; 
    public static final String XA_KEY = "xa";
    DataSource create(DataSourceFactory dsf, Properties config) throws SQLException;
}
