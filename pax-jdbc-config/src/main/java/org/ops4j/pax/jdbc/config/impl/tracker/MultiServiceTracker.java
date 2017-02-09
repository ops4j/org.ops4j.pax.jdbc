/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.jdbc.config.impl.tracker;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({
    "rawtypes", "unchecked"
})
public class MultiServiceTracker implements AutoCloseable {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private Map<Class<?>, ServiceTracker> trackers;
    private AtomicInteger present;
    private BundleContext context;
    private TrackerCallback callback;
    protected Closeable closeable;
    private Map<Class<?>, Object> services;

    public MultiServiceTracker(BundleContext context, TrackerCallback callback) {
        trackers = new HashMap<>();
        services = new HashMap<>();
        this.context = context;
        this.callback = callback;
        present = new AtomicInteger(0);
    }

    public void track(final Class<?> iface, final Filter filter) throws InvalidSyntaxException {
        log.info("Tracking service {} with filter {}", iface.getName(), filter);
        ServiceTracker tracker = new ServiceTracker(context, filter, null) {

            public Object addingService(ServiceReference reference) {
                log.info("Found service {} with filter {}", iface.getName(), filter);
                Object service = super.addingService(reference);
                services.put(iface, service);
                if (present.incrementAndGet() == trackers.size()) {
                    closeable = callback.activate(MultiServiceTracker.this);
                }
                return service; 
            }

            public void removedService(ServiceReference reference, Object service) {
                log.info("Lost service {} with filter {}", iface.getName(), filter);
                Object oldService = services.get(iface);
                if (service == oldService) {
                    services.remove(iface);
                }
                if (present.decrementAndGet() == trackers.size() - 1) {
                    try {
                        closeable.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                super.removedService(reference, service);
            }

        };
        trackers.put(iface, tracker);
    }

    public void open() {
        for (ServiceTracker tracker : trackers.values()) {
            tracker.open();
        }
    }

    public <T> T getService(Class<T> iface) {
        return (T)services.get(iface);
    }

    @Override
    public void close() {
        for (ServiceTracker tracker : trackers.values()) {
            tracker.close();
        }
    }
}
