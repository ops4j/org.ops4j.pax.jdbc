/*
 * Copyright 2021 OPS4J.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.jdbc.jtds.impl;

import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import net.sourceforge.jtds.jdbc.Driver;
import net.sourceforge.jtds.jdbcx.JtdsDataSource;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JTDSDataSourceFactoryTest {

    public static final Logger LOG = LoggerFactory.getLogger(JTDSDataSourceFactoryTest.class);

    /*
        $ podman run -itd --name pax.jdbc.sqlserver -e 'ACCEPT_EULA=Y' -e 'SA_PASSWORD=PaxJdbc!(*67' -e 'MSSQL_PID=Express' \
            -p 1433:1433 -d mcr.microsoft.com/mssql/server:2022-latest

        $ podman logs -f pax.jdbc.sqlserver
        SQL Server 2022 will run as non-root by default.
        This container is running as user mssql.
        ...
        2023-07-25 17:22:07.53 spid39s     Server is listening on [ ::1 <ipv6> 1431] accept sockets 1.
        2023-07-25 17:22:07.53 spid39s     Server is listening on [ 127.0.0.1 <ipv4> 1431] accept sockets 1.
        ...
     */

    private static final String DB = "msdb";
    private static final String SERVER = "localhost";
    private static final String PORT = "1433";
    private static final String USER = "sa";
    private static final String PASSWORD = "PaxJdbc!(*67";

    @Test
    public void testDS() throws SQLException {
        JTDSDataSourceFactory dsf = new JTDSDataSourceFactory();
        Properties props = testProps();
        JtdsDataSource ds = dsf.createDataSource(props);
        validateDS(ds);
    }

    @Test
    public void testConnectionPoolDS() throws SQLException {
        JTDSDataSourceFactory dsf = new JTDSDataSourceFactory();
        Properties props = testProps();
        JtdsDataSource ds = dsf.createConnectionPoolDataSource(props);
        validateDS(ds);
    }

    @Test
    public void testXADS() throws SQLException {
        JTDSDataSourceFactory dsf = new JTDSDataSourceFactory();
        Properties props = testProps();
        JtdsDataSource ds = dsf.createXADataSource(props);
        validateDS(ds);
    }

    @Test
    public void testDriver() {
        JTDSDataSourceFactory dsf = new JTDSDataSourceFactory();
        Properties props = testProps();
        Driver driver = dsf.createDriver(props);
        assertNotNull(driver);
    }

    @Test
    public void testEmptyProps() throws SQLException {
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

    @Test
    public void testPropsWithUrl() throws SQLException {
        JTDSDataSourceFactory dsf = new JTDSDataSourceFactory();
        Properties props = new Properties();
        props.put("databaseName", DB);
        props.put("user", USER);
        props.put("password", PASSWORD);
        props.put("url", "jdbc:jtds:sqlserver://localhost:1433/to-be-overwritten;domain=my-domain;useNTLMv2=true");
        JtdsDataSource ds = dsf.createDataSource(props);
        validateDS(ds);
        assertEquals("my-domain", ds.getDomain());
        assertTrue(ds.getUseNTLMV2());
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

}
