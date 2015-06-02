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
import org.ops4j.pax.jdbc.pool.dbcp2.impl.ds.CloseableManagedDataSource;
import org.ops4j.pax.jdbc.pool.dbcp2.impl.ds.CloseablePoolingDataSource;
import org.ops4j.pax.jdbc.pool.dbcp2.impl.ds.DbcpPooledDataSourceFactory;
import org.ops4j.pax.jdbc.pool.dbcp2.impl.ds.DbcpXAPooledDataSourceFactory;
import org.osgi.service.jdbc.DataSourceFactory;

public class PooledDataSourceFactoryTest {

    @Test
    public void testcreateDataSourceWithXA() throws SQLException {
        IMocksControl c = EasyMock.createControl();
        DataSourceFactory dsf = c.createMock(DataSourceFactory.class);
        XADataSource xads = c.createMock(XADataSource.class);
        EasyMock.expect(dsf.createXADataSource(EasyMock.anyObject(Properties.class))).andReturn(xads).atLeastOnce();
        TransactionManager tm = c.createMock(TransactionManager.class);
        DbcpXAPooledDataSourceFactory pdsf = new DbcpXAPooledDataSourceFactory(dsf, tm);
        c.replay();
        DataSource ds = pdsf.createDataSource(createValidProps());
        c.verify();
        Assert.assertEquals(CloseableManagedDataSource.class, ds.getClass());
        
        try {
            pdsf.createDataSource(createInvalidPoolConfig());
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Error setting property dummy:No setter in class org.apache.commons.pool2.impl.GenericObjectPoolConfig for property dummy", e.getMessage());
        }

        try {
            pdsf.createDataSource(createInvalidFactoryConfig());
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Error setting property dummy:No setter in class org.apache.commons.dbcp2.managed.PoolableManagedConnectionFactory for property dummy", e.getMessage());
        }
    }

    @Test
    public void testcreateDataSource() throws SQLException {
        IMocksControl c = EasyMock.createControl();
        DataSourceFactory dsf = c.createMock(DataSourceFactory.class);
        DataSource exds = c.createMock(DataSource.class);
        EasyMock.expect(dsf.createDataSource(EasyMock.anyObject(Properties.class))).andReturn(exds).atLeastOnce();
        DbcpPooledDataSourceFactory pdsf = new DbcpPooledDataSourceFactory(dsf);

        c.replay();
        DataSource ds = pdsf.createDataSource(createValidProps());
        c.verify();
        Assert.assertEquals(CloseablePoolingDataSource.class, ds.getClass());
        
        try {
            pdsf.createDataSource(createInvalidPoolConfig());
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Error setting property dummy:No setter in class org.apache.commons.pool2.impl.GenericObjectPoolConfig for property dummy", e.getMessage());
        }

        try {
            pdsf.createDataSource(createInvalidFactoryConfig());
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Error setting property dummy:No setter in class org.apache.commons.dbcp2.PoolableConnectionFactory for property dummy", e.getMessage());
        }
    }
    
    private Properties createValidProps() {
        Properties props = new Properties();
        props.put("pool.maxTotal", "8");
        props.put("factory.validationQuery", "dummyQuery");
        return props;
    }

    private Properties createInvalidPoolConfig() {
        Properties props = new Properties();
        props.put("pool.dummy", "8");
        return props;
    }

    private Properties createInvalidFactoryConfig() {
        Properties props2 = new Properties();
        props2.put("factory.dummy", "8");
        return props2;
    }

}
