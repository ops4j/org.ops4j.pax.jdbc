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

import java.net.URL;
import java.sql.Driver;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.ops4j.pax.swissbox.core.BundleClassLoader;
import org.ops4j.pax.swissbox.extender.BundleObserver;
import org.ops4j.spi.SafeServiceLoader;
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
        log.info( "found JDBC driver service in bundle [{} {}]", bundle.getSymbolicName(),
            bundle.getVersion() );
        BundleContext bc = bundle.getBundleContext();
        
        SafeServiceLoader serviceLoader = new SafeServiceLoader( new BundleClassLoader( bundle ) );
        List<Driver> drivers = serviceLoader.load( Driver.class.getName() );
        for (Driver driver : drivers) {
            DriverDataSourceFactory dsf = new DriverDataSourceFactory( driver );
            Dictionary<String, String> props = new Hashtable<String, String>();
            props.put( DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, driver.getClass().getName() );
            bc.registerService( DataSourceFactory.class.getName(), dsf, props );            
        }
    }

    @Override
    public void removingEntries( Bundle bundle, List<URL> entries )
    {
        log.info( "removing drivers registered on behalf of bundle {} {}",
            bundle.getSymbolicName(), bundle.getVersion() );
        // services get unregistered automatically when the extended bundle stops
    }
}
