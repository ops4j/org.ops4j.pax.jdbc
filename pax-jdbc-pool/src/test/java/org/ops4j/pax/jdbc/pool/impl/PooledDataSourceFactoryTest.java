package org.ops4j.pax.jdbc.pool.impl;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jdbc.DataSourceFactory;

public class PooledDataSourceFactoryTest {

    @Test
    public void testcreateDataSourceWithXA() throws SQLException {
        IMocksControl c = EasyMock.createControl();
        DataSourceFactory dsf = c.createMock(DataSourceFactory.class);
        XADataSource xads = c.createMock(XADataSource.class);
        EasyMock.expect(dsf.createXADataSource(EasyMock.anyObject(Properties.class))).andReturn(xads);
        BundleContext context = c.createMock(BundleContext.class);
        expectTransactionManagerLookup(c, context);
        PooledDataSourceFactory pdsf = new PooledDataSourceFactory(dsf , context);
        c.replay();
        
        Properties props = new Properties();
        DataSource ds = pdsf.createDataSource(props);

        c.verify();
        Assert.assertEquals(CloseablePoolingDataSource.class, ds.getClass());
    }
    
    @Test
    public void testcreateDataSource() throws SQLException {
        IMocksControl c = EasyMock.createControl();
        DataSourceFactory dsf = c.createMock(DataSourceFactory.class);
        DataSource exds = c.createMock(DataSource.class);
        EasyMock.expect(dsf.createDataSource(EasyMock.anyObject(Properties.class))).andReturn(exds);
        BundleContext context = c.createMock(BundleContext.class);
        PooledDataSourceFactory pdsf = new PooledDataSourceFactory(dsf , context);
        c.replay();
        
        Properties props = new Properties();
        props.put("pool.usexa", "false");
        DataSource ds = pdsf.createDataSource(props);
        
        c.verify();
        Assert.assertEquals(CloseablePoolingDataSource.class, ds.getClass());
    }

    @SuppressWarnings("unchecked")
    private void expectTransactionManagerLookup(IMocksControl c, BundleContext context) {
        ServiceReference<TransactionManager> ref = c.createMock(ServiceReference.class);
        EasyMock.expect(context.getServiceReference(TransactionManager.class)).andReturn(ref);
        TransactionManager tm = c.createMock(TransactionManager.class);
        EasyMock.expect(context.getService(ref)).andReturn(tm);
    }
}
