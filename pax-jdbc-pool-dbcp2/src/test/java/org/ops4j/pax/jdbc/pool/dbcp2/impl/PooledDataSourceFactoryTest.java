package org.ops4j.pax.jdbc.pool.dbcp2.impl;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Assert;
import org.junit.Test;
import org.ops4j.pax.jdbc.pool.dbcp2.impl.ds.CloseablePoolingDataSource;
import org.ops4j.pax.jdbc.pool.dbcp2.impl.ds.PooledDataSourceFactory;
import org.osgi.service.jdbc.DataSourceFactory;

public class PooledDataSourceFactoryTest {

    @Test
    public void testcreateDataSourceWithXA() throws SQLException {
        IMocksControl c = EasyMock.createControl();
        DataSourceFactory dsf = c.createMock(DataSourceFactory.class);
        XADataSource xads = c.createMock(XADataSource.class);
        EasyMock.expect(dsf.createXADataSource(EasyMock.anyObject(Properties.class))).andReturn(xads);
        TransactionManager tm = c.createMock(TransactionManager.class);
        PooledDataSourceFactory pdsf = new PooledDataSourceFactory(dsf , tm);
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
        PooledDataSourceFactory pdsf = new PooledDataSourceFactory(dsf);
        c.replay();
        
        Properties props = new Properties();
        DataSource ds = pdsf.createDataSource(props);
        
        c.verify();
        Assert.assertEquals(CloseablePoolingDataSource.class, ds.getClass());
    }

}
