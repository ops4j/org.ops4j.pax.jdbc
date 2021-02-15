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
package org.ops4j.pax.jdbc.hook;

import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Register implementations of this interface as an OSGi service to be called by pax-jdbc-config before the 
 * DataSource is published.
 * A possible application is to do a database migration to a newly deployed code version.
 * 
 * The PreHook service must be named using a service property "name". 
 * In the DataSource config the hook to be called must be configured using the property preHook=myname using the name
 * of the registered PerHook service.
 */
public interface PreHook {

    /**
     * Service property key to name a PreHook service
     */
    String KEY_NAME = "name";
    
    /**
     * Config key to refer to a PreHook service
     */
    String CONFIG_KEY_NAME = "ops4j.preHook";
    
    /**
     * Will be called before publishing the DataSource
     * 
     * @param ds data source to work on
     * @throws SQLException in case of exception the DataSource will not be published
     */
    void prepare(DataSource ds) throws SQLException;

}
