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
package org.ops4j.pax.jdbc.test.h2;

import java.lang.reflect.Field;
import java.util.Dictionary;
import java.util.Hashtable;
import javax.inject.Inject;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.jdbc.test.AbstractJdbcTest;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.options;

public class H2PooledDataSourceTest extends AbstractJdbcTest {

    @Inject
    private BundleContext context;

    @Configuration
    public Option[] config() {
        return options(regressionDefaults(), //
            poolDefaults(),
            mvnBundle("org.ops4j.pax.jdbc", "pax-jdbc"), //
            mvnBundle("org.ops4j.pax.jdbc", "pax-jdbc-config"), //
            mvnBundle("org.ops4j.pax.jdbc", "pax-jdbc-pool-dbcp2"), //
            mvnBundle("org.apache.commons", "commons-pool2"), //
            mvnBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.cglib"), //
            mvnBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.jasypt"), //
            mvnBundle("org.apache.commons", "commons-dbcp2"), //
            mvnBundle("com.h2database", "h2") //
        );
    }

    @Test
    public void registerNativeDataSourceAndExpectPooledDataSource() throws Exception {
        JdbcDataSource xaDs = new JdbcDataSource();
        xaDs.setURL("jdbc:h2:mem:pax");

        Dictionary<String, Object> properties = new Hashtable<>();
        // needed for wrapping by pax-jdbc-config
        properties.put("pool", "dbcp2");
        // needed by pax-jdbc-config
        properties.put("osgi.jndi.service.name", "jdbc/h2xads");
        properties.put("pool.maxTotal", "42");
        context.registerService(XADataSource.class, xaDs, properties);

        String filter1 = "(&(objectClass=javax.sql.XADataSource)(pool=dbcp2)(!(pax.jdbc.managed=true)))";
        ServiceTracker<XADataSource, XADataSource> trackerForDatabaseSpecificDs
                = new ServiceTracker<XADataSource, XADataSource>(context, context.createFilter(filter1), null);
        trackerForDatabaseSpecificDs.open();
        XADataSource ds1 = trackerForDatabaseSpecificDs.waitForService(5000);
        assertNotNull(ds1);
        assertTrue(ds1 instanceof JdbcDataSource);
        assertThat(((JdbcDataSource) ds1).getURL(), equalTo("jdbc:h2:mem:pax"));

        // a bit of knowledge about select pool's internals
        String filter2 = "(&(objectClass=javax.sql.DataSource)(pax.jdbc.managed=true))";
        ServiceTracker<DataSource, DataSource> trackerForPool
                = new ServiceTracker<DataSource, DataSource>(context, context.createFilter(filter2), null);
        trackerForPool.open();
        DataSource ds2 = trackerForPool.waitForService(5000);
        assertNotNull(ds2);
        assertThat(ds2.getClass().getName(), equalTo("org.apache.commons.dbcp2.managed.ManagedDataSource"));
        Field poolField = ds2.getClass().getSuperclass().getDeclaredField("_pool");
        poolField.setAccessible(true);
        Object pool = poolField.get(ds2);
        Field maxTotalField = pool.getClass().getSuperclass().getDeclaredField("maxTotal");
        maxTotalField.setAccessible(true);
        Integer maxTotal = (Integer) maxTotalField.get(pool);
        assertThat(maxTotal, equalTo(42));
    }

}
