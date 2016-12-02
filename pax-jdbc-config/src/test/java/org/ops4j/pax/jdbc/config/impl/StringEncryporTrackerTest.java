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

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class StringEncryporTrackerTest {

    @Before
    public void setup() {
        StringEncryptorTracker.ENCRYPTOR_SERVICE_TIMEOUT = 100L;
    }

    @Test
    public void testStringEncryptorWithAlias() throws InvalidSyntaxException {
        IMocksControl c = EasyMock.createControl();

        final String alias = "testAlias";

        final StringEncryptor stringEncryptor = new TestStringEncryptor();

        BundleContext context = c.createMock(BundleContext.class);
        EasyMock.expect(context.getService(EasyMock.anyObject(ServiceReference.class))).andReturn(stringEncryptor);
        EasyMock.expect(context.createFilter(EasyMock.anyString())).andReturn(c.createMock(Filter.class));

        ServiceReference reference = c.createMock(ServiceReference.class);
        EasyMock.expect(reference.getProperty(EasyMock.eq(StringEncryptorTracker.ALIAS_PROPERTY_KEY))).andReturn(alias).atLeastOnce();

        c.replay();

        StringEncryptorTracker tracker = new StringEncryptorTracker(context);
        tracker.addingService(reference);

        final StringEncryptor availableStringEncryptor = tracker.getStringEncryptor(alias);

        c.verify();

        assertEquals(stringEncryptor, availableStringEncryptor);
    }

    @Test
    public void testLazyStringEncryptor() throws InvalidSyntaxException {
        IMocksControl c = EasyMock.createControl();

        final String alias = "testAlias";

        final StringEncryptor stringEncryptor = new TestStringEncryptor();

        final BundleContext context = c.createMock(BundleContext.class);
        EasyMock.expect(context.getService(EasyMock.anyObject(ServiceReference.class))).andReturn(stringEncryptor);
        EasyMock.expect(context.createFilter(EasyMock.anyString())).andReturn(c.createMock(Filter.class));

        final ServiceReference reference = c.createMock(ServiceReference.class);
        EasyMock.expect(reference.getProperty(EasyMock.eq(StringEncryptorTracker.ALIAS_PROPERTY_KEY))).andReturn(alias).atLeastOnce();

        StringEncryptorTracker.ENCRYPTOR_SERVICE_TIMEOUT = 10000L;
        final long waitingTime = 800L;

        c.replay();

        final StringEncryptorTracker tracker = new StringEncryptorTracker(context);

        final long startTs = System.currentTimeMillis();
        
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(waitingTime);
                    tracker.addingService(reference);
                } catch (InterruptedException ex) {
                }
            }
        });
        t.start();

        final StringEncryptor availableStringEncryptor = tracker.getStringEncryptor(alias);
        
        final long endTs = System.currentTimeMillis();

        c.verify();
        
        final long duration = endTs - startTs;
        
        // tolerance: delay of service + 100 ms + 10%, it should be less than timeout of the service tracker
        Assert.assertTrue(duration < (waitingTime + 100) * 1.1);
        
        assertEquals(stringEncryptor, availableStringEncryptor);
    }
    
    @Test
    public void testReconfiguredStringEncryptor() throws InvalidSyntaxException {
        IMocksControl c = EasyMock.createControl();

        final String alias = "testAlias";
        final String alias2 = "testAlias2";

        final StringEncryptor stringEncryptor = new TestStringEncryptor();

        final BundleContext context = c.createMock(BundleContext.class);
        EasyMock.expect(context.getService(EasyMock.anyObject(ServiceReference.class))).andReturn(stringEncryptor);
        EasyMock.expect(context.createFilter(EasyMock.anyString())).andReturn(c.createMock(Filter.class));

        final ServiceReference originalReference = c.createMock(ServiceReference.class);
        EasyMock.expect(originalReference.getProperty(EasyMock.eq(StringEncryptorTracker.ALIAS_PROPERTY_KEY))).andReturn(alias).atLeastOnce();
        
        final ServiceReference modifiedReference = c.createMock(ServiceReference.class);
        EasyMock.expect(modifiedReference.getProperty(EasyMock.eq(StringEncryptorTracker.ALIAS_PROPERTY_KEY))).andReturn(alias2).atLeastOnce();

        StringEncryptorTracker.ENCRYPTOR_SERVICE_TIMEOUT = 10000L;
        final long waitingTime = 800L;

        c.replay();

        final StringEncryptorTracker tracker = new StringEncryptorTracker(context);
        tracker.addingService(originalReference);

        final long startTs = System.currentTimeMillis();
        
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(waitingTime);
                    tracker.modifiedService(modifiedReference, stringEncryptor);
                } catch (InterruptedException ex) {
                }
            }
        });
        t.start();

        final StringEncryptor availableStringEncryptor = tracker.getStringEncryptor(alias2);
        
        final long endTs = System.currentTimeMillis();

        c.verify();
        
        final long duration = endTs - startTs;
        
        // tolerance: delay of service + 100 ms + 10%, it should be less than timeout of the service tracker
        Assert.assertTrue(duration < (waitingTime + 100) * 1.1);
        
        assertEquals(stringEncryptor, availableStringEncryptor);
    }

    @Test
    public void testStringEncryptorWithoutAlias() throws InvalidSyntaxException {
        IMocksControl c = EasyMock.createControl();

        final String alias = null;

        final StringEncryptor stringEncryptor = new TestStringEncryptor();

        BundleContext context = c.createMock(BundleContext.class);
        EasyMock.expect(context.getService(EasyMock.anyObject(ServiceReference.class))).andReturn(stringEncryptor);
        EasyMock.expect(context.createFilter(EasyMock.anyString())).andReturn(c.createMock(Filter.class));

        ServiceReference reference = c.createMock(ServiceReference.class);
        EasyMock.expect(reference.getProperty(EasyMock.eq(StringEncryptorTracker.ALIAS_PROPERTY_KEY))).andReturn(alias).atLeastOnce();

        c.replay();

        StringEncryptorTracker tracker = new StringEncryptorTracker(context);
        tracker.addingService(reference);

        final StringEncryptor availableStringEncryptor = tracker.getStringEncryptor(alias);

        c.verify();

        assertEquals(stringEncryptor, availableStringEncryptor);
    }

    @Test
    public void testMissingStringEncryptor() throws InvalidSyntaxException {
        IMocksControl c = EasyMock.createControl();

        final String alias = "testAlias";
        final String alias2 = "testAlias2";

        final StringEncryptor stringEncryptor = new TestStringEncryptor();

        BundleContext context = c.createMock(BundleContext.class);
        EasyMock.expect(context.getService(EasyMock.anyObject(ServiceReference.class))).andReturn(stringEncryptor);
        EasyMock.expect(context.createFilter(EasyMock.anyString())).andReturn(c.createMock(Filter.class));

        ServiceReference reference = c.createMock(ServiceReference.class);
        EasyMock.expect(reference.getProperty(EasyMock.eq(StringEncryptorTracker.ALIAS_PROPERTY_KEY))).andReturn(alias).atLeastOnce();

        c.replay();

        StringEncryptorTracker tracker = new StringEncryptorTracker(context);
        tracker.addingService(reference);

        final StringEncryptor availableStringEncryptor = tracker.getStringEncryptor(alias2);

        c.verify();

        Assert.assertNull(availableStringEncryptor);
    }

    private static final class TestStringEncryptor implements StringEncryptor {

        private final BasicTextEncryptor textEncryptor;

        public TestStringEncryptor() {
            textEncryptor = new BasicTextEncryptor();
            textEncryptor.setPassword("myPassword");
        }

        @Override
        public String decrypt(String cipherText) {

            return textEncryptor.decrypt(cipherText);
        }

        @Override
        public String encrypt(String decipherText) {
            return textEncryptor.encrypt(decipherText);
        }

    }
}
