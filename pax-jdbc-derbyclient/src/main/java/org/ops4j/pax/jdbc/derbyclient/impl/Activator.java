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
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.derby.drda.NetworkServerControl;
import org.apache.derby.jdbc.ClientDriver;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.jdbc.DataSourceFactory;

public class Activator implements BundleActivator {

    private Map<String, Map<Integer, NetworkServerControl>> startedServers = new HashMap<String, Map<Integer, NetworkServerControl>>();
    private static Activator _instance;

    @Override
    public void start(BundleContext context) throws Exception {
        DerbyClientDatasourceFactory dsf = new DerbyClientDatasourceFactory();
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, ClientDriver.class.getName());
        props.put(DataSourceFactory.OSGI_JDBC_DRIVER_NAME, "derbyclient");
        context.registerService(DataSourceFactory.class.getName(), dsf, props);
        _instance = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        for (Map<Integer, NetworkServerControl> controls : getInstance().startedServers.values()) {
            for (NetworkServerControl control : controls.values()) {
                control.shutdown();
            }
        }
        getInstance().startedServers.clear();

    }

    protected static Activator getInstance() {
        return _instance;
    }

    protected Map<String, Collection<Integer>> getStartedServers() {
        Map<String, Collection<Integer>> ret = new HashMap<String, Collection<Integer>>();
        for (Entry<String, Map<Integer, NetworkServerControl>> keys : startedServers.entrySet()) {
            ret.put(keys.getKey(), keys.getValue().keySet());

        }
        return ret;
    }

    protected void addNetworkControl(String host, int port, NetworkServerControl control) {
        Map<Integer, NetworkServerControl> candidate = startedServers.get(host);
        if (candidate == null) {
            candidate = new HashMap<Integer, NetworkServerControl>();
            startedServers.put(host, candidate);
        }
        candidate.put(port, control);
    }

}
