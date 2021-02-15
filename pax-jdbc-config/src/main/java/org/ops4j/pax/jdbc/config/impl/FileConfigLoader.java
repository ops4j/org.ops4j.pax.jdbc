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
package org.ops4j.pax.jdbc.config.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.ops4j.pax.jdbc.config.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileConfigLoader implements ConfigLoader {

    private static final Logger LOG = LoggerFactory.getLogger(FileConfigLoader.class);

    @Override
    public String getName() {
        return "FILE";
    }

    @Override
    public String resolve(String key) {
        return readFile(key, Charset.defaultCharset());
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
}
