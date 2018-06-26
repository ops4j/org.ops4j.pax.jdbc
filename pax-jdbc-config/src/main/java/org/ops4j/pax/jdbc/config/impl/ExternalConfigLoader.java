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
package org.ops4j.pax.jdbc.config.impl;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ops4j.pax.jdbc.config.ConfigLoader;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * External configuration loader, can be used for Docker secrets too.
 */
public class ExternalConfigLoader {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalConfigLoader.class);
    private static final Pattern CONFIG_LOADER_PATTERN = Pattern.compile("^([^(]+)\\((.+)\\)$");

    private final ServiceTracker<?, ?> tracker;
    private final Map<String, ConfigLoader> configLoaders = new ConcurrentHashMap<>();

    public ExternalConfigLoader(BundleContext context) {
        tracker = ServiceTrackerHelper.helper(context).track(
                ConfigLoader.class,
                "(" + Constants.OBJECTCLASS + "=" + ConfigLoader.class.getName() + ")",
                configLoader -> configLoaders.put(configLoader.getName(), configLoader),
                configLoader -> configLoaders.remove(configLoader.getName()));
    }

    public void destroy() {
        tracker.close();
    }

    /**
     * Resolve external configuration value references.
     *
     * @param config configuration to load external references
     * @return loaded configuration
     */
    @SuppressWarnings("rawtypes")
    public Dictionary<String, Object> resolve(final Dictionary config) {
        Dictionary<String, Object> loadedConfig = new Hashtable<>();
        for (Enumeration e = config.keys(); e.hasMoreElements();) {
            final String key = (String) e.nextElement();
            String value = String.valueOf(config.get(key));
            if (config.get(key) instanceof String && isExternal(value)) {
                Matcher matcher = CONFIG_LOADER_PATTERN.matcher(value);
                matcher.matches();
                String loadedValue = "ENC".equals(matcher.group(1)) ? value : configLoaders.get(matcher.group(1)).resolve(matcher.group(2));
                if (loadedValue != null) {
                    loadedConfig.put(key, loadedValue);
                }
            } else {
                loadedConfig.put(key, config.get(key));
            }
        }
        return loadedConfig;
    }

    /**
     * Check whether a value is external reference.
     *
     * @param value configuration value
     * @return <code>true</code> if value is external reference, <code>false</code> otherwise
     */
    private boolean isExternal(String value) {
        return CONFIG_LOADER_PATTERN.matcher(value).matches();
    }
}
