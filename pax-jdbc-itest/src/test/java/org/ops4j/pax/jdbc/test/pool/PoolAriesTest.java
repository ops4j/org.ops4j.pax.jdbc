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
package org.ops4j.pax.jdbc.test.pool;

import javax.inject.Inject;

import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.Filter;
import org.ops4j.pax.jdbc.pool.common.PooledDataSourceFactory;
import org.ops4j.pax.jdbc.test.AbstractJdbcTest;

public class PoolAriesTest extends AbstractJdbcTest {

    @Inject @Filter("(pool=aries)(xa=false)")
    PooledDataSourceFactory pool;
    
    @Configuration
    public Option[] config() {
        return new Option[] { //
                regressionDefaults(), //
                poolDefaults(), //
                mvnBundle("commons-logging", "commons-logging"),
                mvnBundle("org.apache.geronimo.components", "geronimo-connector"),
                mvnBundle("org.apache.geronimo.specs", "geronimo-j2ee-connector_1.6_spec"),
                mvnBundle("org.ops4j.pax.jdbc", "pax-jdbc-pool-aries"),
                };
    }

    @Test
    public void testPooledDataSourceFactoryServicesPresent() {
    }

}
