package org.ops4j.pax.jdbc.hsqldb.impl;

import java.util.Dictionary;
import java.util.Hashtable;

import org.hsqldb.jdbc.JDBCDriver;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.jdbc.DataSourceFactory;

public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        HsqldbDataSourceFactory dsf = new HsqldbDataSourceFactory();
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, JDBCDriver.class.getName());
        props.put(DataSourceFactory.OSGI_JDBC_DRIVER_NAME, "hsqldb");
        context.registerService(DataSourceFactory.class.getName(), dsf, props);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // empty
    }
}
