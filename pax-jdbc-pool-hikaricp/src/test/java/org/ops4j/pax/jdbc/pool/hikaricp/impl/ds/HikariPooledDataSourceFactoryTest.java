package org.ops4j.pax.jdbc.pool.hikaricp.impl.ds;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.service.jdbc.DataSourceFactory;

import com.zaxxer.hikari.HikariDataSource;

public class HikariPooledDataSourceFactoryTest {

    @Test
    public void testcreateDataSource() throws SQLException {
        IMocksControl c = EasyMock.createControl();
        DataSourceFactory dataSourceFactory = c.createMock(DataSourceFactory.class);
        DataSource dataSource = c.createMock(DataSource.class);

        dataSource.setLoginTimeout(30);
        EasyMock.expectLastCall().anyTimes();

        Connection connection = c.createMock(Connection.class);


        EasyMock.expect(connection.isValid(EasyMock.anyInt())).andReturn(true).anyTimes();

        connection.setAutoCommit(true);
        EasyMock.expectLastCall().anyTimes();
        
        connection.setReadOnly(false);
        EasyMock.expectLastCall().anyTimes();
        
        EasyMock.expect(connection.getTransactionIsolation()).andReturn(Connection.TRANSACTION_NONE).anyTimes();
        
        connection.clearWarnings();
        EasyMock.expectLastCall().anyTimes();
      
        
        EasyMock.expect(dataSource.getConnection()).andReturn(connection).anyTimes();

        EasyMock.expect(dataSourceFactory.createDataSource(EasyMock.anyObject(Properties.class)))
                .andReturn(dataSource).atLeastOnce();

        HikariPooledDataSourceFactory pdsf = new HikariPooledDataSourceFactory(dataSourceFactory);

        c.replay();
        DataSource ds = pdsf.createDataSource(createValidProps());
        c.verify();
        Assert.assertEquals(HikariDataSource.class, ds.getClass());
        
        Assert.assertEquals(((HikariDataSource)ds).getMaximumPoolSize(), 8);

        try {
            pdsf.createDataSource(createInvalidPoolConfig());
        } catch (RuntimeException e) {
            Assert.assertEquals(
                    "java.beans.IntrospectionException: Method not found: setDummy",
                    e.getMessage());
        }

     
    }

    private Properties createValidProps() {
        Properties props = new Properties();
        props.put("hikari.maximumPoolSize", "8");
        return props;
    }

    private Properties createInvalidPoolConfig() {
        Properties props = new Properties();
        props.put("hikari.dummy", "8");
        return props;
    }

}
