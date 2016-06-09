package org.ops4j.pax.jdbc.config.impl;

import java.util.*;

import org.jasypt.encryption.StringEncryptor;
import org.osgi.util.tracker.ServiceTracker;

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
public class Decryptor {
    private static final String ENCRYPTED_PROPERTY_PREFIX = "ENC(";
    private static final String ENCRYPTED_PROPERTY_SUFFIX = ")";
    private ServiceTracker encryptorServiceTracker;
    
    public Decryptor(ServiceTracker encryptorServiceTracker) {
        this.encryptorServiceTracker = encryptorServiceTracker;
    }

    @SuppressWarnings("rawtypes")
    public Dictionary<String, String> decrypt(final Dictionary config) {
        StringEncryptor encryptor = null;
        Dictionary<String, String> decryptedConfig = new Hashtable<String, String>();
        for (Enumeration e = config.keys(); e.hasMoreElements();) {
            final String key = (String) e.nextElement();
            String value = String.valueOf(config.get(key));
            if (config.get(key) instanceof String && isEncrypted(value)){
            	String cipherText = value.substring(ENCRYPTED_PROPERTY_PREFIX.length(),
                		value.length() - ENCRYPTED_PROPERTY_SUFFIX.length());
                if(encryptor == null) {
                    try {
                        encryptor = (StringEncryptor) this.encryptorServiceTracker.waitForService(30000);
                    } catch (InterruptedException e1) {
                        /* ignore */
                    }
                }
                if (encryptor != null) {
                    String plainText = encryptor.decrypt(cipherText);
                    decryptedConfig.put(key, plainText);
                }
            } else {
                decryptedConfig.put(key,value);
            }
        }
        return decryptedConfig;
    }

    public boolean isEncrypted(String value) {
        return value.startsWith(ENCRYPTED_PROPERTY_PREFIX)
                && value.endsWith(ENCRYPTED_PROPERTY_SUFFIX);
    }
}
