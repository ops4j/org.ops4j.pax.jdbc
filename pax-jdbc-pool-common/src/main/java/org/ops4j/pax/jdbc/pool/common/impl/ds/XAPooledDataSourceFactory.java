package org.ops4j.pax.jdbc.pool.common.impl.ds;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;

import org.osgi.service.jdbc.DataSourceFactory;

public abstract class XAPooledDataSourceFactory extends PooledDataSourceFactory {

    protected TransactionManager tm;

    /**
     * Initialize XA PoolingDataSourceFactory
     * 
     * @param dsFactory
     *            non pooled DataSourceFactory we delegate to
     * @param tm
     *            transaction manager (Only needed for XA mode)
     */
    public XAPooledDataSourceFactory(DataSourceFactory dsFactory, TransactionManager tm) {
        super(dsFactory);
        this.tm = tm;
    }

    @Override
    public DataSource createDataSource(Properties props) throws SQLException {
        try {
            XADataSource ds = dsFactory.createXADataSource(getNonPoolProps(props));
            Iterable<Object> dsConfiguration = internalCreateDatasource(ds);

            BeanConfig.configure(dsConfiguration.iterator().next(), getPoolProps(props));
            DataSource wrappedDs = doStart(dsConfiguration);
            return wrappedDs;
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
