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

import java.sql.Driver;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.ServiceLoader;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator, BundleTrackerCustomizer<List<ServiceRegistration<DataSourceFactory>>> {

    private static Logger log = LoggerFactory.getLogger(Activator.class);
    public static final String BUNDLE_NAME = "org.ops4j.pax.jdbc";

    private BundleTracker<List<ServiceRegistration<DataSourceFactory>>> tracker;

    @Override
    public void start(final BundleContext bc) throws Exception {
        log.debug("starting bundle {}", BUNDLE_NAME);
        tracker = new BundleTracker<>(bc, Bundle.ACTIVE, this);
        tracker.open();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        log.debug("stopping bundle {}", BUNDLE_NAME);
        tracker.close();
    }

    @Override
    public List<ServiceRegistration<DataSourceFactory>> addingBundle(Bundle bundle, BundleEvent event) {
        if (bundle.getBundleId() == 0) {
            return null;
        }
        try {
            ServiceReference<?>[] registered = bundle.getRegisteredServices();
            ClassLoader cl = bundle.adapt(BundleWiring.class).getClassLoader();
            ServiceLoader<Driver> drivers = ServiceLoader.load(Driver.class, cl);
            List<ServiceRegistration<DataSourceFactory>> registrations = new ArrayList<>();
            for (Driver driver : drivers) {
                boolean alreadyRegistered = false;
                if (registered != null) {
                    for (ServiceReference<?> ref : registered) {
                        if (isSameDataSourceFactory(driver, ref)) {
                            alreadyRegistered = true;
                            break;
                        }
                    }
                }
                if (!alreadyRegistered) {
                    DriverDataSourceFactory dsf = new DriverDataSourceFactory(driver);
                    Dictionary<String, String> props = new Hashtable<>();
                    props.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, driver.getClass().getName());
                    if (bundle.getSymbolicName() != null) {
                        props.put(DataSourceFactory.OSGI_JDBC_DRIVER_NAME, bundle.getSymbolicName());
                    }
                    if (bundle.getVersion() != null) {
                        props.put(DataSourceFactory.OSGI_JDBC_DRIVER_VERSION, bundle.getVersion().toString());
                    }
                    ServiceRegistration<DataSourceFactory> reg = bundle.getBundleContext().registerService(DataSourceFactory.class, dsf, props);
                    registrations.add(reg);
                }
            }
            return registrations.isEmpty() ? null : registrations;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void modifiedBundle(Bundle bundle, BundleEvent event, List<ServiceRegistration<DataSourceFactory>> object) {
    }

    @Override
    public void removedBundle(Bundle bundle, BundleEvent event, List<ServiceRegistration<DataSourceFactory>> object) {
        for (ServiceRegistration<DataSourceFactory> reg : object) {
            reg.unregister();
        }
    }

    private boolean isSameDataSourceFactory(Driver driver, ServiceReference<?> ref) {
        Object names = ref.getProperty(Constants.OBJECTCLASS);
        return (names instanceof String && DataSourceFactory.class.getName().equals(names)
                || names instanceof String[] && DataSourceFactory.class.getName().equals(((String[])names)[0]))
                && driver.getClass().getName().equals(ref.getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS));
    }

}
