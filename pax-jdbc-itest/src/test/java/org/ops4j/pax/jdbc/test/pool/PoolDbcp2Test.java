/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.jdbc.test.pool;

import javax.inject.Inject;

import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.jdbc.pool.common.PooledDataSourceFactory;
import org.ops4j.pax.jdbc.test.AbstractJdbcTest;

public class PoolDbcp2Test extends AbstractJdbcTest {

    @Inject @org.ops4j.pax.exam.util.Filter("(pool=dbcp2)(xa=true)")
    PooledDataSourceFactory xaPool;
    
    @Inject @org.ops4j.pax.exam.util.Filter("(pool=dbcp2)(xa=false)")
    PooledDataSourceFactory pool;

    @Configuration
    public Option[] config() {
        return new Option[] {
            regressionDefaults(), //
            mvnBundle("com.h2database", "h2"), //
            mvnBundle("org.apache.commons", "commons-pool2"), //
            mvnBundle("commons-logging", "commons-logging"), //
            mvnBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.cglib"), //
            mvnBundle("org.apache.geronimo.specs", "geronimo-jta_1.1_spec"), //
            mvnBundle("org.apache.commons", "commons-dbcp2"), //
            mvnBundle("org.apache.aries", "org.apache.aries.util"), //
            mvnBundle("org.apache.aries.transaction", "org.apache.aries.transaction.manager"), //
            mvnBundle("org.ops4j.pax.jdbc", "pax-jdbc-pool-common"), //
            mvnBundle("org.ops4j.pax.jdbc", "pax-jdbc-pool-dbcp2"), //
        };
    }
    
    @Test
    public void testPooledDataSourceFactoryServicesPresent() {
    }

}
