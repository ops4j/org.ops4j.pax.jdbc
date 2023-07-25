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
package org.ops4j.pax.jdbc.sqlite.impl;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.ops4j.pax.jdbc.common.BeanConfig;
import org.osgi.service.jdbc.DataSourceFactory;
import org.sqlite.JDBC;
import org.sqlite.SQLiteDataSource;

public class SqliteDataSourceFactory implements DataSourceFactory {

	private final class SQLiteDataSourceExtension extends SQLiteDataSource {
		private String username;
		private String password;

		public SQLiteDataSourceExtension(String username, String password) {
			this.username = username;
			this.password = password;
		}

		@Override
		public Connection getConnection() throws SQLException {
			return super.getConnection(username, password);
		}
	}

	@Override
	public DataSource createDataSource(Properties props) throws SQLException {
		String username = removeProperty(props, JDBC_USER);
		String password = removeProperty(props, JDBC_PASSWORD);
		SQLiteDataSource dataSource = new SQLiteDataSourceExtension(username, password);
		if (props != null) {
			String url = props.getProperty(JDBC_URL);
			if (url == null) {
				dataSource.setUrl("jdbc:sqlite:" + props.getProperty(JDBC_DATABASE_NAME));
				props.remove(JDBC_DATABASE_NAME);
			} else {
				dataSource.setUrl(url);
				props.remove(JDBC_URL);
			}
			if (!props.isEmpty()) {
				BeanConfig.configure(dataSource, props, false);
			}
		}
		return dataSource;
	}

    private String removeProperty(Properties props, String property) {
    	if (props != null) {
    		String value = props.getProperty(property);
    		props.remove(property);
    		return value;
    	}
		return null;
	}

	@Override
    public ConnectionPoolDataSource createConnectionPoolDataSource(Properties props)
        throws SQLException {
        throw new SQLException();
    }

    @Override
    public XADataSource createXADataSource(Properties props) throws SQLException {
        throw new SQLException();
    }

    @Override
    public Driver createDriver(Properties props) throws SQLException {
        JDBC driver = new JDBC();
        return driver;
    }
}
