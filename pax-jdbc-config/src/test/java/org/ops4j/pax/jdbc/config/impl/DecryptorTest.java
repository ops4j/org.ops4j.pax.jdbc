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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.Test;
import org.osgi.util.tracker.ServiceTracker;

/**
 * 
 * @author kameshs
 *
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class DecryptorTest {

	@Test
	public void testDecryptWithNoEncryptedProperties() {
		Dictionary dsProps = new Hashtable<>();
		dsProps.put("dataSourceName", "testDS");
		dsProps.put("timeout", 2000);

		IMocksControl c = EasyMock.createControl();
		ServiceTracker serviceTracker = c.createMock(ServiceTracker.class);

		Decryptor decryptor = new Decryptor(serviceTracker);
		Dictionary decryptedConfig = decryptor.decrypt(dsProps);

		for (Enumeration e = decryptedConfig.keys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			String expectedValue = String.valueOf(dsProps.get(key));
			String actualValue = String.valueOf(decryptedConfig.get(key));
			assertEquals(expectedValue, actualValue);
		}
	}

	@Test
	public void testDecryptWithEncryptedProperties(){

		final String myPassword = "password";
		final TestStringEnryptor testStringEnryptor = new TestStringEnryptor();
		String encryptedPassword = testStringEnryptor.encrypt(myPassword);

		Dictionary dsProps = new Hashtable<>();
		dsProps.put("dataSourceName", "testDS");
		dsProps.put("password", "ENC("+encryptedPassword+")");
		dsProps.put("timeout", 2000);

		IMocksControl c = EasyMock.createControl();
		ServiceTracker serviceTracker = c.createMock(ServiceTracker.class);

		try {
			EasyMock.expect(serviceTracker
					.waitForService(30000)).andReturn(testStringEnryptor).atLeastOnce();
		} catch (InterruptedException e) {
			
		}
		
		c.replay();

		Decryptor decryptor = new Decryptor(serviceTracker);
		Dictionary decryptedConfig = decryptor.decrypt(dsProps);
		c.verify();
		
		assertEquals("testDS", decryptedConfig.get("dataSourceName"));
		assertEquals("password", decryptedConfig.get("password"));
		assertEquals("2000", decryptedConfig.get("timeout"));


	}

	@Test
	public void testIsEncrypted() {
		IMocksControl c = EasyMock.createControl();

		String value = "ENC(123456abce)";

		ServiceTracker serviceTracker = c.createMock(ServiceTracker.class);
		Decryptor decryptor = new Decryptor(serviceTracker);

		boolean isEncrypted = decryptor.isEncrypted(value);
		assertTrue(isEncrypted);

		value = "123456abce";
		isEncrypted = decryptor.isEncrypted(value);
		assertFalse(isEncrypted);
	}

	private static final class TestStringEnryptor implements StringEncryptor{

		private final BasicTextEncryptor textEncryptor;

		public TestStringEnryptor() {
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
