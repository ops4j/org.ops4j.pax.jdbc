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
package org.ops4j.pax.jdbc.common;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Configure a java bean from a given Map of properties.
 */
public class BeanConfig {

    private final Object bean;
    private final Map<String, Method> setters;

    private BeanConfig(Object bean) {
        this.bean = bean;
        this.setters = findSettersForBean(bean);
    }

    private static Map<String, Method> findSettersForBean(Object bean) {
        Map<String, Method> setters = new HashMap<String, Method>();
        for (Method method : bean.getClass().getMethods()) {
            String name = method.getName();
            if (name.startsWith("set") && method.getParameterTypes().length == 1) {
                String key1 = name.substring(3, 4).toLowerCase() + name.substring(4);
                String key2 = name.substring(3, 4) + name.substring(4);
                setters.put(key1, method);
                setters.put(key2, method);
            }
        }
        return setters;
    }

    /**
     * Configure a java bean from a given {@link Properties}.
     * 
     * @param bean
     *            bean to populate
     * @param props
     *            properties to set.
     */
    public static void configure(Object bean, Properties props) {
    	configure(bean, props, true);
    }
    public static void configure(Object bean, Properties props, boolean failOnMissing) {
        final Map<String, String> map = new HashMap<>();
        for (String key : props.stringPropertyNames()) {
            map.put(key, props.getProperty(key));
        }
        BeanConfig.configure(bean, map, failOnMissing);
    }

    /**
     * Configure a java bean from a given Map of properties.
     *
     * @param bean
     *            bean to populate
     * @param props
     *            properties to set. The keys in the Map have to match the bean property names.
     */
    public static void configure(Object bean, Map<String, String> props) {
    	
    }
    public static void configure(Object bean, Map<String, String> props, boolean failOnMissing) {
        BeanConfig beanConfig = new BeanConfig(bean);
        for (String key : props.keySet()) {
            beanConfig.trySetProperty(key, props.get(key), failOnMissing);
        }
    }

    private void trySetProperty(String key, String value, boolean failOnMissing) {
        try {
            Method method = setters.get(key);
            if (method == null) {
            	if (failOnMissing) {
                throw new IllegalArgumentException("No setter in " + bean.getClass()
                    + " for property " + key);
            	}
            	return;
            }
            Class<?> paramClass = method.getParameterTypes()[0];
            if (paramClass == int.class || paramClass == Integer.class) {
                method.invoke(bean, Integer.parseInt(value));
            }
            else if (paramClass == long.class || paramClass == Long.class) {
                method.invoke(bean, Long.parseLong(value));
            }
            else if (paramClass == boolean.class || paramClass == Boolean.class) {
                method.invoke(bean, Boolean.parseBoolean(value));
            }
            else if (paramClass == String.class) {
                method.invoke(bean, value);
            }
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Error setting property " + key + ":"
                + e.getMessage(), e);
        }
    }

}
