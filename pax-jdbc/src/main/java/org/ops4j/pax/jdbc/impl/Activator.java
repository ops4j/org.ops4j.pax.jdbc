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

import org.ops4j.pax.swissbox.extender.BundleURLScanner;
import org.ops4j.pax.swissbox.extender.BundleWatcher;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static Logger log = LoggerFactory.getLogger(Activator.class);
    public static final String BUNDLE_NAME = "org.ops4j.pax.jdbc";

    private BundleWatcher<URL> watcher;

    @Override
    @SuppressWarnings("unchecked")
    public void start(BundleContext bc) throws Exception {
        log.debug("starting bundle {}", BUNDLE_NAME);
        BundleURLScanner scanner = new BundleURLScanner("META-INF/services", "java.sql.Driver",
            false);
        JdbcDriverObserver observer = new JdbcDriverObserver();
        watcher = new BundleWatcher<URL>(bc, scanner, observer);
        watcher.start();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        log.debug("stopping bundle {}", BUNDLE_NAME);
        watcher.stop();
    }
}
