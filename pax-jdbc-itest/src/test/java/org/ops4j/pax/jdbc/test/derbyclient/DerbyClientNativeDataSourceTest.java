/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ops4j.pax.jdbc.test.derbyclient;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assume.assumeThat;
import static org.ops4j.pax.exam.CoreOptions.options;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.derby.drda.NetworkServerControl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.util.Filter;
import org.ops4j.pax.jdbc.derbyclient.constants.ClientConnectionConstant;
import org.ops4j.pax.jdbc.test.AbstractJdbcTest;
import org.ops4j.pax.jdbc.test.ServerConfiguration;
import org.osgi.service.jdbc.DataSourceFactory;

@RunWith(PaxExam.class)
public class DerbyClientNativeDataSourceTest extends AbstractJdbcTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Inject
    @Filter(value = "(osgi.jdbc.driver.name=derbyclient)")
    private DataSourceFactory dsf;
    
    private ServerConfiguration dbConfig = new ServerConfiguration("derbyclient");

    public void startDerbyServer() throws Exception {
        InetAddress addr = Inet4Address.getByName(dbConfig.getServerName());
        Integer port = new Integer(dbConfig.getPortNumber());
        NetworkServerControl server = new NetworkServerControl(addr, port);
        server.start(null);
    }

    @Configuration
    public Option[] config() throws Exception {
        startDerbyServer();
        return options(regressionDefaults(), //
            mvnBundle("org.apache.derby", "derbyclient"), //
            mvnBundle("org.ops4j.pax.jdbc", "pax-jdbc-derbyclient") //
        );
    }

    @Test
    public void createDataSourceAndConnection() throws SQLException, InterruptedException {
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_DATABASE_NAME, dbConfig.getDatabaseName());
        props.setProperty(DataSourceFactory.JDBC_USER, dbConfig.getUser());
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, dbConfig.getPassword());
        props.setProperty(DataSourceFactory.JDBC_PORT_NUMBER, dbConfig.getPortNumberSt());
        props.setProperty(DataSourceFactory.JDBC_SERVER_NAME, dbConfig.getServerName());
        props.setProperty(ClientConnectionConstant.CREATE_DATABASE, "create");
        dsf.createDataSource(props).getConnection().close();
    }

    @Test
    public void connectWithDefaultPort() throws SQLException {
        assumeThat(dbConfig.getPortNumber(), is(1527));
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_DATABASE_NAME, dbConfig.getDatabaseName());
        props.setProperty(DataSourceFactory.JDBC_USER, dbConfig.getUser());
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, dbConfig.getPassword());
        props.setProperty(DataSourceFactory.JDBC_SERVER_NAME, dbConfig.getServerName());
        props.setProperty(ClientConnectionConstant.CREATE_DATABASE, "create");
        dsf.createDataSource(props).getConnection().close();
    }

    @Test
    public void connectWithDefaultHostAndPort() throws SQLException {
        assumeThat(dbConfig.getPortNumber(), is(1527));
        assumeThat(dbConfig.getServerName(), is("localhost"));
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_DATABASE_NAME, dbConfig.getDatabaseName());
        props.setProperty(DataSourceFactory.JDBC_USER, dbConfig.getUser());
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, dbConfig.getPassword());
        props.setProperty(ClientConnectionConstant.CREATE_DATABASE, "create");
        dsf.createDataSource(props).getConnection().close();
    }

}
