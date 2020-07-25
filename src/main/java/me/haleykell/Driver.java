package me.haleykell;

import me.haleykell.userdata.User;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

import java.sql.*;
import java.util.Scanner;

/**
 * This program allows the user to store and retrieve usernames and passwords. This is helpful for people who use lots
 * of different passwords or who struggle to remember passwords.
 *
 * @author Haley Kell
 */

public class Driver {

    public static void main(String[] args) {

        String url = "jdbc:sqlite:sqlite/db/password_manager.db";

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("Connection to SQLite established.");

                ResultSet tables = meta.getTables(null, null, "versions", null);
                if (tables.next()) {
                    // Database exists
                    try (PreparedStatement stmt = conn.prepareStatement("SELECT MAX(version) as version FROM versions")) {
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) System.out.println("Database found. Version: " + rs.getInt("version"));
                    }
                } else {
                    createTables(conn);
                    System.out.println("Database initialized.");
                }

                Scanner input = new Scanner(System.in);
                User user;
                Pbkdf2PasswordEncoder pwEncoder = new Pbkdf2PasswordEncoder();
                pwEncoder.setAlgorithm(Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);

                System.out.println("Are you a new user? Y/N");
                String yn = input.next();
                if (yn.equalsIgnoreCase("y")) user = User.registerNewUser(conn, input, pwEncoder);
                else user = User.login(conn, input, pwEncoder);

                if (user == null) throw new RuntimeException("Error while logging in.");
                System.out.println("User " + user.getUsername() + " is now logged in.");

                Menu menu = new Menu(conn, input, user);
                menu.menu();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("Data saved.");
    }

    public static void createTables(Connection conn) throws SQLException {
        // Create tables
        try (PreparedStatement stmt = conn.prepareStatement("CREATE TABLE versions (version integer)")) {
            stmt.executeUpdate();
            try (PreparedStatement st = conn.prepareStatement("INSERT INTO versions(version) VALUES(?)")) {
                st.setInt(1, 1);
                st.executeUpdate();
            }
            System.out.println("Database versions table created.");
        }

        // Create users table
        try (PreparedStatement stmt = conn.prepareStatement("CREATE TABLE users\n" +
                "(\n" +
                "    id                   INTEGER      NOT NULL,\n" +
                "    username             VARCHAR(128) NOT NULL,\n" +
                "    email                VARCHAR(128) NOT NULL,\n" +
                "\n" +
                "    master_password_hash BINARY       NOT NULL,\n" +
                "    password_key         BINARY       NOT NULL,\n" +
                "\n" +
                "    PRIMARY KEY (id)\n" +
                ")")) {
            stmt.executeUpdate();
            System.out.println("User table created.");
        }

        // Create website data table
        try (PreparedStatement stmt = conn.prepareStatement("CREATE TABLE website_data\n" +
                "(\n" +
                "    website_data_id INTEGER      NOT NULL,\n" +
                "    website         VARCHAR(512) NOT NULL,\n" +
                "    username        VARCHAR(128) NOT NULL,\n" +
                "    password        BINARY       NOT NULL,\n" +
                "\n" +
                "    PRIMARY KEY (website_data_id)\n" +
                ")")) {
            stmt.executeUpdate();
            System.out.println("Website data table created.");
        }

        // Create passwords table
        try (PreparedStatement stmt = conn.prepareStatement("CREATE TABLE passwords\n" +
                "(\n" +
                "    user_id         INTEGER NOT NULL,\n" +
                "    website_data_id INTEGER NOT NULL,\n" +
                "\n" +
                "    PRIMARY KEY (user_id, website_data_id),\n" +
                "    FOREIGN KEY (user_id) REFERENCES users (id),\n" +
                "    FOREIGN KEY (website_data_id) REFERENCES website_data (website_data_id)\n" +
                ")")) {
            stmt.executeUpdate();
            System.out.println("Website data table created.");
        }
    }
}
