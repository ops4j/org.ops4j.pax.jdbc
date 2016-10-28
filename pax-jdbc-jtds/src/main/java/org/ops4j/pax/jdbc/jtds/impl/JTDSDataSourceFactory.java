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
package org.ops4j.pax.jdbc.jtds.impl;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.osgi.service.jdbc.DataSourceFactory;

import net.sourceforge.jtds.jdbc.Driver;
import net.sourceforge.jtds.jdbcx.JtdsDataSource;

public class JTDSDataSourceFactory implements DataSourceFactory {
	private static final List<Method> methods = Arrays.asList(JtdsDataSource.class.getMethods());

    public JTDSDataSourceFactory() {
        super();
    }

    @Override
    public JtdsDataSource createDataSource(Properties props) throws SQLException {
        try {
            return setProperties(new JtdsDataSource(), props);
        }
        catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public JtdsDataSource createConnectionPoolDataSource(Properties props) throws SQLException {
        try {
            return setProperties(new JtdsDataSource(), props);
        }
        catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public JtdsDataSource createXADataSource(Properties props) throws SQLException {
        try {
            return setProperties(new JtdsDataSource(), props);
        }
        catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public Driver createDriver(Properties props) {
    	return new Driver();
    }

    private JtdsDataSource setProperties(JtdsDataSource dsi, Properties props) throws Exception {
    	for (Entry<Object, Object> prop : props.entrySet()) {
    		setProperty(dsi, String.valueOf(prop.getKey()), String.valueOf(prop.getValue()));
    	}
    	return dsi;
    }

    private void setProperty(JtdsDataSource dsi, String key, String value) throws Exception {
        if (value == null) {
            return;
        }

        for (Method method : methods) {
        	if (method.getParameterTypes().length==1 && method.getName().equalsIgnoreCase("set"+key)) {
        		Class<?> type = method.getParameterTypes()[0];
        		if (String.class==type) {
        			method.invoke(dsi, value);
        		} else if (Integer.TYPE==type) {
        			method.invoke(dsi, Integer.parseInt(value));
        		} else if (Long.TYPE==type) {
        			method.invoke(dsi, Long.parseLong(value));
        		} else if (Boolean.TYPE==type) {
        			method.invoke(dsi, Boolean.parseBoolean(value));
        		}
        	}
        }
    }

}
