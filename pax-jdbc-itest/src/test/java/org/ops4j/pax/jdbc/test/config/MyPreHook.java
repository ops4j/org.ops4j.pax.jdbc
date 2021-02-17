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
package org.ops4j.pax.jdbc.test.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.ops4j.pax.jdbc.hook.PreHook;

public class MyPreHook implements PreHook {

    @Override
    public void prepare(DataSource ds) throws SQLException {
        String createQuery = "CREATE TABLE PERSON(id int primary key, name varchar(255))";
        String insertQuery = "INSERT INTO PERSON" + "(id, name) values" + "(?,?)";
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement(createQuery)) {
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
                stmt.setInt(1, 1);
                stmt.setString(2, "Chris");
                stmt.executeUpdate();
            }
        }
    }

}
