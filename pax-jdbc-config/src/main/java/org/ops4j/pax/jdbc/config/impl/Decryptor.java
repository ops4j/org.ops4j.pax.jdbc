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

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import org.jasypt.encryption.StringEncryptor;

/**
 * Decryptor for cipher texts.
 */
public class Decryptor {

    private static final String ENCRYPTED_PROPERTY_PREFIX = "ENC(";
    private static final String ENCRYPTED_PROPERTY_SUFFIX = ")";
    private static final char ALIAS_SEPARATOR = ',';

    private final StringEncryptorTracker encryptorServiceTracker;

    /**
     * Create new decryptor instance.
     *
     * @param tracker custom StringEncryptor tracker the supports aliases
     */
    public Decryptor(final StringEncryptorTracker tracker) {
        this.encryptorServiceTracker = tracker;
    }

    /**
     * Decrypt configuration.
     *
     * @param config configuration to decrypt
     * @return decrypted configuration
     */
    @SuppressWarnings("rawtypes")
    public Dictionary<String, String> decrypt(final Dictionary config) {
        Dictionary<String, String> decryptedConfig = new Hashtable<>();
        for (Enumeration e = config.keys(); e.hasMoreElements();) {
            final String key = (String) e.nextElement();
            String value = String.valueOf(config.get(key));
            if (config.get(key) instanceof String && isEncrypted(value)) {
                final String plainText = decryptValue(value);
                if (plainText != null) {
                    decryptedConfig.put(key, plainText);
                }
            } else {
                decryptedConfig.put(key, value);
            }
        }
        return decryptedConfig;
    }

    /**
     * Decrypt encrypted configuration value. Alias is optional and separated with ALIAS_SEPARATOR character.
     *
     * @param value encrypted configuration value, composite of cipher text and alias
     * @return decrypted (plain text) configuration value
     */
    private String decryptValue(final String value) {
        final String argument = value.substring(ENCRYPTED_PROPERTY_PREFIX.length(),
                value.length() - ENCRYPTED_PROPERTY_SUFFIX.length());
        final int aliasPos = argument.indexOf(ALIAS_SEPARATOR);
        final String cipherText = aliasPos > -1 ? argument.substring(0, aliasPos) : argument;
        final String alias = aliasPos > -1 ? argument.substring(aliasPos + 1).trim() : null;

        StringEncryptor encryptor = encryptorServiceTracker.getStringEncryptor(alias);
        return encryptor != null ? encryptor.decrypt(cipherText) : null;
    }

    /**
     * Check whether a value is encrypted.
     *
     * @param value configuration value
     * @return <code>true</code> if value is encrypted, <code>false</code> otherwise
     */
    public boolean isEncrypted(String value) {
        return value.startsWith(ENCRYPTED_PROPERTY_PREFIX)
                && value.endsWith(ENCRYPTED_PROPERTY_SUFFIX);
    }
}
