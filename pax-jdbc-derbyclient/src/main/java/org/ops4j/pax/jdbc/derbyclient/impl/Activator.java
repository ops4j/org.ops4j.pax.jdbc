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
package org.ops4j.pax.jdbc.derbyclient.impl;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.derby.jdbc.ClientDriver;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.jdbc.DataSourceFactory;

public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        DerbyClientDatasourceFactory dsf = new DerbyClientDatasourceFactory();
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, ClientDriver.class.getName());
        props.put(DataSourceFactory.OSGI_JDBC_DRIVER_NAME, "derbyclient");
        context.registerService(DataSourceFactory.class.getName(), dsf, props);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }

}
