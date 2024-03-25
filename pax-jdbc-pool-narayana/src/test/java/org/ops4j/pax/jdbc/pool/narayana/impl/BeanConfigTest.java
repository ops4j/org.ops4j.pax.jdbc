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
package org.ops4j.pax.jdbc.pool.narayana.impl;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Assert;
import org.junit.Test;
import org.ops4j.pax.jdbc.common.BeanConfig;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class BeanConfigTest {

    @Test
    public void testPoolConfig() throws Exception {
        Map<String, String> props = new HashMap<String, String>();
        props.put("maxIdle", "2");
        props.put("testOnBorrow", "true");
        props.put("maxWaitMillis", "1000");
        props.put("jmxNameBase", "name");
        GenericObjectPoolConfig<?> config = new GenericObjectPoolConfig<>();
        BeanConfig.configure(config, props);
        Assert.assertEquals(2, config.getMaxIdle());
        Assert.assertTrue(config.getTestOnBorrow());
        Assert.assertEquals(1000, config.getMaxWaitMillis());
        Assert.assertEquals("name", config.getJmxNameBase());
    }

    @Test
    public void testPoolableConnectionFactory() throws Exception {
        Map<String, String> props = new HashMap<>();
        props.put("validationQuery", "dummyQuery");
        props.put("validationQueryTimeout", "1");
        PoolableConnectionFactory pcf = new PoolableConnectionFactory(null, null);
        BeanConfig.configure(pcf, props);

        PoolableConnection connection = mock(PoolableConnection.class);

        pcf.validateConnection(connection);
        verify(connection).validate(eq("dummyQuery"), eq(Duration.ofSeconds(1)));
        verify(connection).isClosed();
    }

}
