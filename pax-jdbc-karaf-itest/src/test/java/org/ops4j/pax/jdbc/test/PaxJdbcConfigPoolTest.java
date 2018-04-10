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
package org.ops4j.pax.jdbc.test;

import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;

import java.sql.SQLException;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.Filter;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class PaxJdbcConfigPoolTest extends AbstractJdbcTest {

    @Inject
    @Filter("(osgi.jndi.service.name=withpool)")
    DataSource dataSource;

    @Configuration
    public Option[] config() {
        return new Option[] //
        { //
          karafDefaults(), //
          features(paxJdbcRepo(), "pax-jdbc-h2", "pax-jdbc-config", "pax-jdbc-pool-dbcp2"),
          applyConfig("org.ops4j.datasource-withpool.cfg")
        };
    }

    

    /**
     * Test that pooled DataSource was created from given config and is usable
     */
    @Test
    public void testDataSourceFromConfig() throws SQLException {
        checkDataSource(dataSource);
    }

}
