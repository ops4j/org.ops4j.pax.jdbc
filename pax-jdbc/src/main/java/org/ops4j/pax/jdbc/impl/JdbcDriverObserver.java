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
package org.ops4j.pax.jdbc.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.ops4j.lang.Ops4jException;
import org.ops4j.pax.swissbox.extender.BundleObserver;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcDriverObserver implements BundleObserver<URL>
{

    private static Logger log = LoggerFactory.getLogger( JdbcDriverObserver.class );

    @Override
    public void addingEntries( Bundle bundle, List<URL> entries )
    {
        URL url = entries.get( 0 );
        log.info( "found driver {}", url );

        List<String> names = parse( Driver.class, url );
        for(String impl : names )
        {
            log.info( "driver impl: {}", impl );
            try
            {
                Class<?> driverClass = bundle.loadClass( impl );
                Driver driver = (Driver) driverClass.newInstance();
                BundleContext bc = bundle.getBundleContext();
                DriverDataSourceFactory dsf = new DriverDataSourceFactory( driver );
                Dictionary<String, String> props = new Hashtable<String, String>();
                props.put( DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, impl );
                bc.registerService( DataSourceFactory.class.getName(), dsf, props );

            }
            catch ( ClassNotFoundException exc )
            {
                throw new Ops4jException( exc );
            }
            catch ( InstantiationException exc )
            {
                throw new Ops4jException( exc );
            }
            catch ( IllegalAccessException exc )
            {
                throw new Ops4jException( exc );
            }
        }
    }

    @Override
    public void removingEntries( Bundle bundle, List<URL> entries )
    {
        log.info( "removed driver {}", entries.get( 0 ) );
    }

    private void parseLine( List<String> names, String line )
    {
        int commentPos = line.indexOf( '#' );
        if( commentPos >= 0 )
        {
            line = line.substring( 0, commentPos );
        }
        line = line.trim();
        if( !line.isEmpty() && !names.contains( line ) )
        {
            names.add( line );
        }
    }

    private List<String> parse( Class<?> klass, URL url )
    {
        InputStream is = null;
        BufferedReader reader = null;
        List<String> names = new ArrayList<String>();
        try
        {
            is = url.openStream();
            reader = new BufferedReader( new InputStreamReader( is, "UTF-8" ) );
            String line = null;
            while( ( line = reader.readLine() ) != null )
            {
                parseLine( names, line );
            }
        }
        catch ( IOException x )
        {
            throw new RuntimeException( x );
        }
        finally
        {
            closeSilently( reader, url );
        }
        return names;
    }

    private void closeSilently( BufferedReader reader, URL url )
    {
        try
        {
            if( reader != null )
            {
                reader.close();
            }
        }
        catch ( IOException exc )
        {
            log.error( "cannot close " + url, exc );
        }
    }
}
