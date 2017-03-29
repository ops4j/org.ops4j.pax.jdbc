package org.ops4j.pax.jdbc.test.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.ops4j.pax.jdbc.hook.PreHook;

public class MyPreHook implements PreHook {

    @Override
    public void prepare(DataSource ds) throws SQLException {
        String CreateQuery = "CREATE TABLE PERSON(id int primary key, name varchar(255))";
        String InsertQuery = "INSERT INTO PERSON" + "(id, name) values" + "(?,?)";
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement(CreateQuery)) {
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = connection.prepareStatement(InsertQuery)) {
                stmt.setInt(1, 1);
                stmt.setString(2, "Chris");
                stmt.executeUpdate();
            }
        }
    }

}
