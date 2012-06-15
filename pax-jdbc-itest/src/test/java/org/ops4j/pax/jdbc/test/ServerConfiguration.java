/*
 * Copyright 2012 Harald Wellmann.
 *
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
package org.ops4j.pax.jdbc.test;

import java.io.IOException;
import java.util.Properties;

import org.ops4j.lang.Ops4jException;

public class ServerConfiguration
{
    private String subprotocol;

    private String serverName;
    private String portNumber;
    private String databaseName;
    private String user;
    private String password;

    public ServerConfiguration( String subprotocol )
    {
        this.subprotocol = subprotocol;
        load();
    }

    private void load()
    {
        try
        {
            Properties props = new Properties();
            props.load( getClass().getResourceAsStream( "/jdbc.properties" ) );
            serverName = props.getProperty( key( "serverName" ), "localhost" );
            portNumber = props.getProperty( key( "portNumber" ) );
            databaseName = props.getProperty( key( "databaseName" ) );
            user = props.getProperty( key( "user" ) );
            password = props.getProperty( key( "password" ) );

        }
        catch ( IOException exc )
        {
            throw new Ops4jException( exc );
        }
    }

    private String key( String suffix )
    {
        return String.format("pax.jdbc.%s.%s", subprotocol, suffix);
    }

    /**
     * @return the subprotocol
     */
    public String getSubprotocol()
    {
        return subprotocol;
    }

    /**
     * @return the serverName
     */
    public String getServerName()
    {
        return serverName;
    }

    /**
     * @return the portNumber
     */
    public String getPortNumber()
    {
        return portNumber;
    }

    /**
     * @return the databaseName
     */
    public String getDatabaseName()
    {
        return databaseName;
    }

    /**
     * @return the user
     */
    public String getUser()
    {
        return user;
    }

    /**
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }
    
    /**
     * @return JDBC URL for the configured database (not including user and password)
     */
    public String getUrl()
    {
        String optPort = (portNumber == null) ? "" : (":" + portNumber);
        return String.format("jdbc:%s://%s%s/%s", subprotocol, serverName, optPort, databaseName);
    }
}
