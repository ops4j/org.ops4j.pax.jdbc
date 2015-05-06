package org.ops4j.pax.jdbc.derbyclient.impl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.derby.drda.NetworkServerControl;
import org.apache.derby.jdbc.ClientDataSource;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ops4j.pax.jdbc.derbyclient.constants.ClientConnectionConstant;
import org.osgi.service.jdbc.DataSourceFactory;

public class TestDsf {
    private static NetworkServerControl server;

    @BeforeClass
    public static void createDb() throws Exception {
        server = new NetworkServerControl("localhost" , "1527");
        server.start(null);
    }
    
    @Test
    public void testPropertyBased() throws SQLException, IOException {
        DerbyClientDatasourceFactory dsf = new DerbyClientDatasourceFactory();
        Properties props = new Properties();
        props.put(DataSourceFactory.JDBC_DATABASE_NAME, "target/test1");
        props.put(ClientConnectionConstant.CREATE_DATABASE, "create");
        props.put(DataSourceFactory.JDBC_USER, "derby");
        DataSource ds = dsf.createDataSource(props);
        ClientDataSource eds = (ClientDataSource)ds;
        Assert.assertEquals("target/test1", eds.getDatabaseName());
        Assert.assertEquals("create", eds.getCreateDatabase());
        Connection con = ds.getConnection();
        con.close();
    }
    
    @Test
    public void testUrlBased() throws SQLException, IOException {
        DerbyClientDatasourceFactory dsf = new DerbyClientDatasourceFactory();
        Properties props = new Properties();
        props.put(DataSourceFactory.JDBC_URL, "jdbc:derby:target/test;create=true");
        props.put(DataSourceFactory.JDBC_USER, "derby");
        DataSource ds = dsf.createDataSource(props);
        ClientDataSource eds = (ClientDataSource)ds;
        Assert.assertEquals("target/test", eds.getDatabaseName());
        Assert.assertEquals("create=true", eds.getConnectionAttributes());
        Connection con = ds.getConnection();
        con.close();
    }
    
    @AfterClass
    public static void shutdownDerby() throws Exception {
        server.shutdown();
    }
}
