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
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import javax.sql.DataSource;

import org.junit.Test;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestDsf {

    public static final Logger LOG = LoggerFactory.getLogger(TestDsf.class);

    @Test
    public void testPropertyBased() throws ClassNotFoundException, SQLException {
        DataSourceFactory factory = new SqliteDataSourceFactory();
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_URL, "jdbc:sqlite::memory:");
        DataSource ds = factory.createDataSource(props);
        try (Connection con = ds.getConnection()) {
            DatabaseMetaData md = con.getMetaData();
            LOG.info("DB: {}/{}", md.getDatabaseProductName(), md.getDatabaseProductVersion());

            try (Statement st = con.createStatement()) {
                try (ResultSet rs = st.executeQuery("SELECT * FROM sqlite_master")) {
                    while (rs.next()) {
                        LOG.info("{} | {}", rs.getString(1), rs.getString(2));
                    }
                }
            }
        }
    }

}
