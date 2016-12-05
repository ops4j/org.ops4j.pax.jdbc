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
package org.ops4j.pax.jdbc.config.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * External configuration loader, can be used for Docker secrets too.
 */
public class ExternalConfigLoader {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalConfigLoader.class);

    private static final String EXTERNAL_PROPERTY_PREFIX = "FILE(";
    private static final String EXTERNAL_PROPERTY_SUFFIX = ")";

    /**
     * Resolve external configuration value references.
     *
     * @param config configuration to load external references
     * @return loaded configuration
     */
    @SuppressWarnings("rawtypes")
    public Dictionary<String, String> resolve(final Dictionary config) {
        Dictionary<String, String> loadedConfig = new Hashtable<>();
        for (Enumeration e = config.keys(); e.hasMoreElements();) {
            final String key = (String) e.nextElement();
            String value = String.valueOf(config.get(key));
            if (config.get(key) instanceof String && isExternal(value)) {
                final String argument = value.substring(EXTERNAL_PROPERTY_PREFIX.length(),
                        value.length() - EXTERNAL_PROPERTY_SUFFIX.length());
                final String loadedValue = readFile(argument, Charset.defaultCharset());
                if (loadedValue != null) {
                    loadedConfig.put(key, loadedValue);
                }
            } else {
                loadedConfig.put(key, value);
            }
        }
        return loadedConfig;
    }

    /**
     * Load file contents and return it as String.
     *
     * @param path file path
     * @param encoding encoding charset
     * @return file contents
     */
    private static String readFile(String path, Charset encoding) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            return new String(encoded, encoding);
        } catch (IOException ex) {
            LOG.error("Unable to read external configuration from " + path, ex);
            return null;
        }
    }

    /**
     * Check whether a value is external reference.
     *
     * @param value configuration value
     * @return <code>true</code> if value is external reference, <code>false</code> otherwise
     */
    private static boolean isExternal(String value) {
        return value.startsWith(EXTERNAL_PROPERTY_PREFIX)
                && value.endsWith(EXTERNAL_PROPERTY_SUFFIX);
    }
}
