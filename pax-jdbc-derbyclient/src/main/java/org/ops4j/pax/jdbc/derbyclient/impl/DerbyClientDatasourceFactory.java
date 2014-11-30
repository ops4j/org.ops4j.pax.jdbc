package org.ops4j.pax.jdbc.derbyclient.impl;

/*
 * #%L
 * Wrapper for apache derby database
 * %%
 * Copyright (C) 2013 - 2014 Osgiliath
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.PrintWriter;
import java.net.InetAddress;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.apache.derby.drda.NetworkServerControl;
import org.apache.derby.jdbc.ClientConnectionPoolDataSource40;
import org.apache.derby.jdbc.ClientDataSource;
import org.apache.derby.jdbc.ClientDataSource40;
import org.apache.derby.jdbc.ClientDriver40;
import org.apache.derby.jdbc.ClientXADataSource40;
import org.ops4j.pax.jdbc.derbyclient.constants.ClientConnectionConstant;
import org.osgi.service.jdbc.DataSourceFactory;

public class DerbyClientDatasourceFactory implements DataSourceFactory {

    // private Logger LOG = LoggerFactory.getLogger(DerbyClientDatasourceFactory.class);

    @Override
    public DataSource createDataSource(Properties props) throws SQLException {
        ClientDataSource40 ds = new ClientDataSource40();
        setProperties(ds, props);
        return ds;
    }

    private void setProperties(ClientDataSource ds, Properties properties) throws SQLException {
        Properties props = (Properties) properties.clone();
        String doStartServer = (String) props.remove(ClientConnectionConstant.AUTO_START_SERVER);
        if (doStartServer != null) {
            if (Boolean.parseBoolean(doStartServer)) {
                doStartServer(props);
            }
        }
        String databaseName = (String) props.remove(DataSourceFactory.JDBC_DATABASE_NAME);
        if (databaseName == null) {
            throw new SQLException("missing required property "
                + DataSourceFactory.JDBC_DATABASE_NAME);
        }
        ds.setDatabaseName(databaseName);

        String password = (String) props.remove(DataSourceFactory.JDBC_PASSWORD);
        ds.setPassword(password);

        String user = (String) props.remove(DataSourceFactory.JDBC_USER);
        ds.setUser(user);

        String createDatabase = (String) props.remove(ClientConnectionConstant.CREATE_DATABASE);
        ds.setCreateDatabase(createDatabase);
        String host = (String) properties.remove(DataSourceFactory.JDBC_SERVER_NAME);
        if (host == null) {
            host = "localhost";
        }

        ds.setServerName(host);
        String portNumber = (String) props.remove(DataSourceFactory.JDBC_PORT_NUMBER);
        if (portNumber != null) {
            ds.setPortNumber(Integer.parseInt(portNumber));
        }
        else {
            ds.setPortNumber(1527);
        }
    }

    private void doStartServer(Properties properties) {
        String host = (String) properties.get(DataSourceFactory.JDBC_SERVER_NAME);
        if (host == null) {
            host = "localhost";
        }
        String portNumberS = (String) properties.get(DataSourceFactory.JDBC_PORT_NUMBER);
        int portNumber = portNumberS == null ? 1527 : Integer.parseInt(portNumberS);
        boolean alreadyStarted = false;
        if (Activator.getInstance().getStartedServers().containsKey(host)) {
            alreadyStarted = Activator.getInstance().getStartedServers().get(host)
                .contains(portNumber);
        }
        if (!alreadyStarted) {
            try {
                InetAddress adress = InetAddress.getByName(host);
                NetworkServerControl control = new NetworkServerControl(adress, portNumber);
                String writer = (String) properties.remove(ClientConnectionConstant.LOG_FILE);
                if (writer == null) {
                    writer = "derbyServer.log";
                }
                PrintWriter printWriter = new PrintWriter(writer);
                control.start(printWriter);
                Activator.getInstance().addNetworkControl(host, portNumber, control);
            }
            catch (Exception e) {
                // LOG.error("Error creating host adress", e);
            }
        }

    }

    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource(Properties props)
        throws SQLException {
        ClientConnectionPoolDataSource40 ds = new ClientConnectionPoolDataSource40();
        setProperties(ds, props);
        return ds;
    }

    @Override
    public XADataSource createXADataSource(Properties props) throws SQLException {
        ClientXADataSource40 ds = new ClientXADataSource40();
        setProperties(ds, props);
        ds.setConnectionAttributes("autoCommit=false");
        return ds;
    }

    @Override
    public Driver createDriver(Properties props) throws SQLException {
        ClientDriver40 driver = new ClientDriver40();

        return driver;
    }

}
