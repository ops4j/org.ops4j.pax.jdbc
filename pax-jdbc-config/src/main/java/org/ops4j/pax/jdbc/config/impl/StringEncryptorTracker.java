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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import org.jasypt.encryption.StringEncryptor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * StringEncryptor service tracker. It is a wrapper to get service for a given alias.
 */
public class StringEncryptorTracker extends ServiceTracker {

    /**
     * OSGi property key used for StringEncryptor aliases.
     */
    public static final String ALIAS_PROPERTY_KEY = "alias";

    private static final Logger LOG = LoggerFactory.getLogger(StringEncryptorTracker.class);

    private final transient Map<String, StringEncryptor> encryptors = Collections.synchronizedSortedMap(new TreeMap<String, StringEncryptor>());

    static long ENCRYPTOR_SERVICE_TIMEOUT = 30000L;

    /**
     * Create new StringEncryptor service tracker instance.
     *
     * @param context OSGi bundle context
     */
    public StringEncryptorTracker(final BundleContext context) {
        super(context, StringEncryptor.class.getName(), null);
    }

    /**
     * Add a StringEncryptor service and store it for Decryptor too. Waiting threads are notified too for a given alias.
     *
     * @param reference OSGi service reference
     * @return OSGi service
     */
    @Override
    public Object addingService(final ServiceReference reference) {
        final String key = getAlias(reference);
        final StringEncryptor stringEncryptor = (StringEncryptor) super.addingService(reference);

        encryptors.put(key, stringEncryptor);

        final Object lockObject = LockObject.get(key);
        synchronized (lockObject) {
            lockObject.notify();
        }

        return stringEncryptor;
    }

    /**
     * Modify a StringEncryptor service. Internal store is also updated because alias could be changed. Waiting threads
     * are notified too for a given alias.
     *
     * @param reference OSGi service reference
     * @param service OSGi service
     */
    @Override
    public void modifiedService(final ServiceReference reference, Object service) {
        final String key = getAlias(reference);

        final StringEncryptor stringEncryptor = (StringEncryptor) service;

        for (Iterator<Map.Entry<String, StringEncryptor>> it = encryptors.entrySet().iterator(); it.hasNext();) {
            final Map.Entry<String, StringEncryptor> entry = it.next();
            if (service.equals(entry.getValue())) {
                it.remove();
            }
        }

        encryptors.put(key, stringEncryptor);

        final Object lockObject = LockObject.get(key);
        synchronized (lockObject) {
            lockObject.notify();
        }

        super.modifiedService(reference, service);
    }

    /**
     * Remove a StringEncryptor service.
     *
     * @param reference OSGi service reference
     * @param service OSGi service
     */
    @Override
    public void removedService(final ServiceReference reference, Object service) {
        final String key = getAlias(reference);
        encryptors.remove(key);

        super.removedService(reference, service);
    }

    /**
     * Get internal store key for the given service reference. Is it calculated based on OSGi service property (alias).
     *
     * @param reference OSGi service reference
     * @return key for internal store
     */
    private String getAlias(final ServiceReference reference) {
        final Object aliasProp = reference.getProperty(ALIAS_PROPERTY_KEY);
        return getKey(aliasProp != null ? aliasProp.toString() : null);
    }

    /**
     * Create internal store key for a given alias. 'X' character is used of null alias, 'A' prefix is added otherwise
     * as internal store key.
     *
     * @param alias OSGi service property (alias)
     * @return internal store key
     */
    private static String getKey(final String alias) {
        return alias != null ? "A" + alias : "X";
    }

    /**
     * Get StringEncryptor for a given alias. Thread is waiting for a specific time if service is not available yet.
     *
     * @param alias alias of the StringEncryptor (null value is supported too)
     * @return StringEncryptor instance
     */
    public StringEncryptor getStringEncryptor(final String alias) {
        final String key = getKey(alias);
        StringEncryptor encryptor = encryptors.get(key);

        if (encryptor == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Waiting for StringEncryptor with alias: " + alias);
            }
            final LockObject lockObject = LockObject.get(key);
            synchronized (lockObject) {
                try {
                    lockObject.wait(ENCRYPTOR_SERVICE_TIMEOUT);
                } catch (InterruptedException ex) {
                    LOG.warn("Waiting for String encryptor service is interrupted, alias: " + alias);
                }
            }

            // retry if notified/timed out
            encryptor = encryptors.get(key);

            if (encryptor == null) {
                LOG.warn("StringEncryptor service it not available with alias: " + alias);
            }
        }

        return encryptor;
    }

    /**
     * Cleanup cached services.
     */
    @Override
    public void close() {
        super.close();
        encryptors.clear();
    }

    /**
     * Lock objects used to notify waiting threads for StringEncryptor with a specific alias.
     */
    private static class LockObject {

        private static final Map<String, LockObject> LOCKS = new HashMap<>();

        /**
         * Get lock object for a given key (alias).
         *
         * @param key StringEncryptor key, null value is not supported
         * @return lock object
         */
        static synchronized LockObject get(final String key) {
            LockObject lock = LOCKS.get(key);
            if (lock == null) {
                lock = new LockObject();
                LOCKS.put(key, lock);
            }
            return lock;
        }
    }
}
