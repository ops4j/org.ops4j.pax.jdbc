/*
 * Licensed under the Apache License, Version 2.0 (the "License");
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

import java.sql.SQLException;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import net.sourceforge.jtds.jdbc.Driver;
import net.sourceforge.jtds.jdbcx.JtdsDataSource;

public class JTDSDataSourceFactoryTest {
    private static final String DB = "testdb";
    private static final String SERVER = "testhost";
    private static final String PORT = "1433";
    private static final String USER = "testuser";
    private static final String PASSWORD = "testpassword";

    @Test
    public void testDS() throws SQLException, ClassNotFoundException {
        JTDSDataSourceFactory dsf = new JTDSDataSourceFactory();
        Properties props = testProps();
        JtdsDataSource ds = dsf.createDataSource(props);
        validateDS(ds);
    }

    @Test
    public void testConnectionPoolDS() throws SQLException, ClassNotFoundException {
        JTDSDataSourceFactory dsf = new JTDSDataSourceFactory();
        Properties props = testProps();
        JtdsDataSource ds = (JtdsDataSource)dsf.createConnectionPoolDataSource(props);
        validateDS(ds);
    }

    @Test
    public void testXADS() throws SQLException, ClassNotFoundException {
        JTDSDataSourceFactory dsf = new JTDSDataSourceFactory();
        Properties props = testProps();
        JtdsDataSource ds = (JtdsDataSource)dsf.createXADataSource(props);
        validateDS(ds);
    }

    @Test
    public void testDriver() throws SQLException, ClassNotFoundException {
        JTDSDataSourceFactory dsf = new JTDSDataSourceFactory();
        Properties props = testProps();
        Driver driver = dsf.createDriver(props);
        Assert.assertNotNull(driver);
    }

    @Test
    public void testEmptyProps() throws SQLException, ClassNotFoundException {
        JTDSDataSourceFactory dsf = new JTDSDataSourceFactory();
        Properties props = new Properties();
        JtdsDataSource ds = dsf.createDataSource(props);
        Assert.assertNotNull(ds);
    }

    private void validateDS(JtdsDataSource ds) {
        Assert.assertEquals(DB, ds.getDatabaseName());
        Assert.assertEquals(SERVER, ds.getServerName());
        Assert.assertEquals(1433, ds.getPortNumber());
        Assert.assertEquals(USER, ds.getUser());
        Assert.assertEquals(PASSWORD, ds.getPassword());
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
