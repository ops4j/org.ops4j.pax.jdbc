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
package org.ops4j.pax.jdbc.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.junit.Assume;
import org.junit.rules.ExternalResource;
import org.ops4j.lang.Ops4jException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default configuration for native container regression tests, overriding the default test system
 * configuration.
 * <p>
 * We do not need the Remote Bundle Context for Native Container, and we prefer unified logging with
 * logback.
 * <p>
 * To override the standard options, you need to set the configuration property
 * {@code pax.exam.system = default}.
 */
public class ServerConfiguration extends ExternalResource {
    private static final Logger LOG = LoggerFactory.getLogger(ServerConfiguration.class);

    private String subprotocol;

    private String serverName;
    private int portNumber;
    private String databaseName;
    private String user;
    private String password;
    private Map<String, Integer> defaultPorts;

    public ServerConfiguration(String subprotocol) {
        this.subprotocol = subprotocol;
        load();
        this.defaultPorts = new java.util.HashMap<String, Integer>();
        this.defaultPorts.put("mysql", 3306);
        this.defaultPorts.put("mariadb", 3306);
        this.defaultPorts.put("postgresql", 3306);
    }

    private void load() {
        try {
            Properties props = new Properties();
            props.load(getClass().getResourceAsStream("/jdbc.properties"));
            serverName = props.getProperty(key("serverName"), "localhost");
            String portNumberSt = props.getProperty(key("portNumber"));
            portNumber = (portNumberSt == null) ? this.defaultPorts.get(subprotocol) : Integer.parseInt(portNumberSt);
            databaseName = props.getProperty(key("databaseName"));
            user = props.getProperty(key("user"));
            password = props.getProperty(key("password"));

        }
        catch (IOException exc) {
            throw new Ops4jException(exc);
        }
    }

    private String key(String suffix) {
        return String.format("pax.jdbc.%s.%s", subprotocol, suffix);
    }

    /**
     * @return the subprotocol
     */
    public String getSubprotocol() {
        return subprotocol;
    }

    /**
     * @return the serverName
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * @return the portNumber
     */
    public int getPortNumber() {
        return portNumber;
    }
    
    /**
     * @return the portNumber
     */
    public String getPortNumberSt() {
        return new Integer(portNumber).toString();
    }

    /**
     * @return the databaseName
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return JDBC URL for the configured database (not including user and password)
     */
    public String getUrl() {
        String optPort = (portNumber == 0) ? "" : (":" + portNumber);
        return String.format("jdbc:%s://%s%s/%s", subprotocol, serverName, optPort, databaseName);
    }
    
    public boolean isAvailable() {
        Socket socket = new Socket();
        try {
            InetSocketAddress endpoint = new InetSocketAddress(serverName, portNumber);
            socket.connect(endpoint, (int) TimeUnit.SECONDS.toMillis(5));
            return true;
        }
        catch (Exception ex) {
            LOG.warn(String.format("cannot connect to %s at %s:%d, ignoring test", subprotocol, 
                                   serverName, portNumber));
            return false;
        }
        finally {
            if (socket != null) {
                try {
                    socket.close();
                }
                catch (IOException e) {

                }
            }
        }
    }

    @Override
    protected void before() throws Throwable {
        Assume.assumeTrue(isAvailable());
    }
}
