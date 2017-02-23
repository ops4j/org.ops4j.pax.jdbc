/*
µ * Licensed under the Apache License, Version 2.0 (the "License");
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
package org.ops4j.pax.jdbc.jtds.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import net.sourceforge.jtds.jdbc.Driver;
import net.sourceforge.jtds.jdbcx.JtdsDataSource;

public class JTDSDataSourceFactoryTest {
    private static final String DB = "testdb";
    private static final String SERVER = "testhost";
    private static final String PORT = "1433";
    private static final String USER = "testuser";
    private static final String PASSWORD = "testpassword";
    private static final String URL = "jdbc:jtds:sqlserver://" + SERVER + ":" + PORT + "/" + DB + ";integratedSecurity=true;domain=testDomain;useNTLMv2=true";

    @Test
    public void testDS() throws SQLException, ClassNotFoundException {
        JTDSDataSourceFactory dsf = new JTDSDataSourceFactory();
        Properties props = testProps();
        JtdsDataSource ds = dsf.createDataSource(props);
        validateDS(ds);
    }
    @Test
    public void testDSWithURL() throws SQLException, ClassNotFoundException {
        JTDSDataSourceFactory dsf = new JTDSDataSourceFactory();
        Properties props = testPropsWithURL();      
        JtdsDataSource ds = dsf.createDataSource(props);
        validateDS(ds);
    }

    @Test
    public void testConnectionPoolDS() throws SQLException, ClassNotFoundException {
        JTDSDataSourceFactory dsf = new JTDSDataSourceFactory();
        Properties props = testProps();
        JtdsDataSource ds = dsf.createConnectionPoolDataSource(props);
        validateDS(ds);
    }
    @Test
    public void testConnectionPoolDSWithURL() throws SQLException, ClassNotFoundException {
        JTDSDataSourceFactory dsf = new JTDSDataSourceFactory();
        Properties props = testPropsWithURL();  
        JtdsDataSource ds = dsf.createConnectionPoolDataSource(props);
        validateDS(ds);
    }

    @Test
    public void testXADS() throws SQLException, ClassNotFoundException {
        JTDSDataSourceFactory dsf = new JTDSDataSourceFactory();
        Properties props = testProps();
        JtdsDataSource ds = dsf.createXADataSource(props);
        validateDS(ds);
    }
    
    @Test
    public void testXADSwithURL() throws SQLException, ClassNotFoundException {
        JTDSDataSourceFactory dsf = new JTDSDataSourceFactory();
        Properties props = testPropsWithURL();
        JtdsDataSource ds = dsf.createXADataSource(props);
        validateDS(ds);
    }

    @Test
    public void testDriver() throws SQLException, ClassNotFoundException {
        JTDSDataSourceFactory dsf = new JTDSDataSourceFactory();
        Properties props = testProps();
        Driver driver = dsf.createDriver(props);
        assertNotNull(driver);
    }
    
    @Test
    public void testDriverWithURL() throws SQLException, ClassNotFoundException {
        JTDSDataSourceFactory dsf = new JTDSDataSourceFactory();
        Properties props = testPropsWithURL();
        Driver driver = dsf.createDriver(props);
        assertNotNull(driver);
    }

    @Test
    public void testEmptyProps() throws SQLException, ClassNotFoundException {
        JTDSDataSourceFactory dsf = new JTDSDataSourceFactory();
        Properties props = new Properties();
        JtdsDataSource ds = dsf.createDataSource(props);
        assertNotNull(ds);
    }

    @Test
    public void testParseUrl() throws Exception {
        JTDSDataSourceFactory dsf = new JTDSDataSourceFactory();
        Map<String, String> result = dsf.parseUrl(null);
        assertTrue(result.isEmpty());

        result = dsf.parseUrl("");
        assertTrue(result.isEmpty());

        result = dsf.parseUrl("jdbc:bla");
        assertTrue(result.isEmpty());

        result = dsf.parseUrl("jdbc:jtds:sqlserver//");
        assertTrue(result.isEmpty());

        result = dsf.parseUrl("jdbc:jtds:sqlserver://host");
        assertEquals("1", result.get("SERVERTYPE"));
        assertEquals("host", result.get("SERVERNAME"));

        result = dsf.parseUrl("jdbc:jtds:sqlserver://host/testdb");
        assertEquals("1", result.get("SERVERTYPE"));
        assertEquals("host", result.get("SERVERNAME"));
        assertEquals("1433", result.get("PORTNUMBER"));
        assertEquals("testdb", result.get("DATABASENAME"));

        result = dsf.parseUrl("jdbc:jtds:sqlserver://host:1434/bla;appName=Test");
        assertEquals("1", result.get("SERVERTYPE"));
        assertEquals("host", result.get("SERVERNAME"));
        assertEquals("1434", result.get("PORTNUMBER"));
        assertEquals("bla", result.get("DATABASENAME"));
        assertEquals("Test", result.get("APPNAME"));
    }


    private void validateDS(JtdsDataSource ds) {
        assertEquals(DB, ds.getDatabaseName());
        assertEquals(SERVER, ds.getServerName());
        assertEquals(1433, ds.getPortNumber());
        assertEquals(USER, ds.getUser());
        assertEquals(PASSWORD, ds.getPassword());
    }

    private Properties testProps() {
        Properties props = new Properties();
        props.put("databaseName", DB);
        props.put("serverName", SERVER);
        props.put("portNumber", PORT);
        props.put("user", USER);
        props.put("password", PASSWORD);
        return props;
    }
    
    private Properties testPropsWithURL() {
        Properties props = testProps();
        props.put("url", URL);
        return props;
    }

}
