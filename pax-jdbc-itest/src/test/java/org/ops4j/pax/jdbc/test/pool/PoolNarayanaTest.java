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
package org.ops4j.pax.jdbc.test.pool;

import javax.inject.Inject;

import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.jdbc.pool.common.PooledDataSourceFactory;
import org.ops4j.pax.jdbc.test.AbstractJdbcTest;

import static org.ops4j.pax.exam.OptionUtils.combine;

public class PoolNarayanaTest extends AbstractJdbcTest {

    @Inject
    @org.ops4j.pax.exam.util.Filter("(pool=narayana)(xa=true)")
    PooledDataSourceFactory xaPool;

    @Inject
    @org.ops4j.pax.exam.util.Filter("(pool=narayana)(xa=false)")
    PooledDataSourceFactory pool;

    @Configuration
    public Option[] config() {
        return combine(
                regressionDefaults(), //
                poolDefaults(), //
                CoreOptions.bootDelegationPackage("sun.*"), //
                mvnBundle("com.h2database", "h2"), //
                mvnBundle("org.apache.commons", "commons-dbcp2"), //
                mvnBundle("org.apache.commons", "commons-pool2"), //
                mvnBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.cglib"), //
                mvnBundle("org.jboss.narayana.osgi", "narayana-osgi-jta"), //
                mvnBundle("org.ops4j.pax.jdbc", "pax-jdbc-pool-narayana") //
        );
    }

    @Test
    public void testPooledDataSourceFactoryServicesPresent() {
    }

}
