package org.ops4j.pax.jdbc.derby.impl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.Assert;
import org.junit.Test;
import org.ops4j.pax.jdbc.derby.constants.ConnectionConstant;
import org.osgi.service.jdbc.DataSourceFactory;

public class TestDsf {
    @Test
    public void testPropertyBased() throws SQLException, IOException {
        DerbyDataSourceFactory dsf = new DerbyDataSourceFactory();
        Properties props = new Properties();
        props.put(DataSourceFactory.JDBC_DATABASE_NAME, "target/test1");
        props.put(ConnectionConstant.CREATE_DATABASE, "create");
        DataSource ds = dsf.createDataSource(props);
        EmbeddedDataSource eds = (EmbeddedDataSource)ds;
        Assert.assertEquals("target/test1", eds.getDatabaseName());
        Assert.assertEquals("create", eds.getCreateDatabase());
        Connection con = ds.getConnection();
        con.close();
    }
    
    @Test
    public void testUrlBased() throws SQLException, IOException {
        DerbyDataSourceFactory dsf = new DerbyDataSourceFactory();
        Properties props = new Properties();
        props.put(DataSourceFactory.JDBC_URL, "jdbc:derby:target/test;create=true");
        DataSource ds = dsf.createDataSource(props);
        EmbeddedDataSource eds = (EmbeddedDataSource)ds;
        Assert.assertEquals("target/test", eds.getDatabaseName());
        Assert.assertEquals("create=true", eds.getConnectionAttributes());
        Connection con = ds.getConnection();
        con.close();
    }
}
