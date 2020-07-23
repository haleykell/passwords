package me.haleykell;

import me.haleykell.userdata.Website;

import java.sql.*;
import java.util.ArrayList;
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
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT * FROM versions")) {
                        if (rs.next()) System.out.println("Database found. Version: " + rs.getInt("version"));
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                } else { // Create tables
                    String version = "CREATE TABLE versions (version integer PRIMARY KEY);";
                    try (Statement st = conn.createStatement()) {
                        st.execute(version);
                        String in = "INSERT INTO versions(version) VALUES(?)";
                        try (PreparedStatement pstmt = conn.prepareStatement(in)) {
                            pstmt.setInt(1, 1);
                            pstmt.executeUpdate();
                            System.out.println("Version inserted.");
                        } catch (SQLException e) {
                            System.out.println(e.getMessage());
                        }
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }

                    // TODO: Create User table


                    // TODO: Create Website table


                    // TODO: Create WebsiteUser table


                    System.out.println("Database created.");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        DataFile data = new DataFile();
        ArrayList<Website> dataList = data.getData();

        Menu menu = new Menu();

        Scanner input = new Scanner(System.in);
        menu.menu(dataList, input);

        data.writeFile(dataList);
        System.out.println("Data saved.");
    }
}
