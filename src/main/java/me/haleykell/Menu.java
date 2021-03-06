package me.haleykell;

import me.haleykell.userdata.User;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

/**
 * Menu class, contains main functionality of the program
 *
 * @author Haley Kell
 */

public class Menu {

    // TODO: error/exception handling

    private static final int GET_INFO = 1;
    private static final int ADD_WEBSITE = 2;
    private static final int DELETE_WEBSITE = 3;
    private static final int CHANGE_USER = 4;
    private static final int CHANGE_PASS = 5;
    private static final int QUIT = 8;
    private static final int PRINT_ALL = 7;
    private static final int DELETE_USER = 6;
    private Connection connection;
    private Scanner input;
    private User user;

    /**
     * Constructor
     */
    public Menu(Connection connection, Scanner input, User user) {
        this.connection = connection;
        this.input = input;
        this.user = user;
    }

    /**
     * Calls showMenu() and appropriate method based on response
     */
    public void menu() throws SQLException {
        int response = showMenu();

        while (response != QUIT) {
            switch (response) {
                case GET_INFO:
                    getInfo();
                    break;
                case ADD_WEBSITE:
                    addWebsite();
                    break;
                case DELETE_WEBSITE:
                    deleteWebsite();
                    break;
                case CHANGE_USER:
                    changeUsername();
                    break;
                case CHANGE_PASS:
                    changePassword();
                    break;
                case DELETE_USER:
                    deleteUser();
                    break;
                case PRINT_ALL:
                    printAllWebsites();
                    break;
                default:
                    System.out.println("Please try again.");
                    break;
            }
            response = showMenu();
        }

    }

    /**
     * Displays menu and receives response
     *
     * @return response of user
     */
    private int showMenu() {
        System.out.println("\nWelcome to PasswordManager!");
        System.out.println("Choose from the following options:");
        System.out.println("1. Get a username and password for a website.");
        System.out.println("2. Add a website's username and password to the database.");
        System.out.println("3. Delete a website's username and password from the database.");
        System.out.println("4. Change a username for a website.");
        System.out.println("5. Change a password for a website");
        System.out.println("6. Delete your user. WARNING: This will delete all of your saved usernames and passwords.");
        System.out.println("7. Print all websites and usernames in the database.");
        System.out.println("8. Quit.");

        return input.nextInt();
    }

    public void printAllWebsites() throws SQLException {
        System.out.println("\nWebsites:");
        System.out.println("---------");
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM website_data\n" +
                "LEFT JOIN passwords p on website_data.website_data_id = p.website_data_id\n" +
                "LEFT JOIN users u on p.user_id = u.id\n" +
                "WHERE u.id like (?)")) {
            stmt.setInt(1, this.user.getId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println("Website: " + rs.getString("website") + "\nUsername: " + rs.getString("username") + "\n");
            }
        }
    }

    /**
     * Displays username and password for a given website
     */
    private void getInfo() throws SQLException {
        printAllWebsites();

        System.out.println("What website do you want to look up?");
        String website = input.next();
        System.out.println("What is the username?");
        String username = input.next();

        try (PreparedStatement stmt = connection.prepareStatement("SELECT password FROM website_data\n" +
                "LEFT JOIN passwords p on website_data.website_data_id = p.website_data_id\n" +
                "LEFT JOIN users u on p.user_id = u.id\n" +
                "WHERE website like (?) AND u.id like (?) AND website_data.username like (?)")) {
            stmt.setString(1, website);
            stmt.setInt(2, this.user.getId());
            stmt.setString(3, username);
            ResultSet rs = stmt.executeQuery();
            String password;
            if (rs.next()) {
                SecretKey key = User.getKey(user.getDecryptedKey());
                try {
                    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
                    cipher.init(Cipher.DECRYPT_MODE, key);
                    password = new String(cipher.doFinal(Base64.getDecoder().decode(rs.getString("password"))));
                    System.out.println("password: " + password);
                } catch (Exception e) {
                    System.out.println("Error while decrypting: " + e.toString());
                }
            } else System.out.println("No passwords found.");
        } catch (SQLException e) {
            System.out.println("Error while retrieving password.");
            throw e;
        }
    }

    /**
     * Adds a website and its info to the list
     */
    private void addWebsite() throws SQLException {
        System.out.println();
        System.out.print("What is the name of the website?\nwebsite: ");
        String website = input.next();
        System.out.print("What is the username?\nusername: ");
        String username = input.next();
        String password;
        int websiteDataId = 0;

        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM website_data\n" +
                "LEFT JOIN passwords p on website_data.website_data_id = p.website_data_id\n" +
                "LEFT JOIN users u on p.user_id = u.id\n" +
                "WHERE u.id like (?) AND website like (?) AND website_data.username like (?)")) {
            stmt.setInt(1, this.user.getId());
            stmt.setString(2, website);
            stmt.setString(3, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                if (rs.getString("username").equalsIgnoreCase(username)) {
                    System.out.println("This website and username combo already exists. Would you like to update the password? Y/N");
                    if (input.next().equalsIgnoreCase("y")) {
                        System.out.print("What is the new password?\npassword: ");
                        password = input.next();
                        websiteDataId = rs.getInt("website_data_id");
                        try (PreparedStatement st = connection.prepareStatement("DELETE FROM website_data " +
                                "WHERE website_data_id like (?)")) {
                            st.setInt(1, websiteDataId);
                            st.executeUpdate();
                        }

                        SecretKey key = User.getKey(user.getDecryptedKey());
                        String encryptedPass = "";
                        try {
                            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                            cipher.init(Cipher.ENCRYPT_MODE, key);
                            encryptedPass = Base64.getEncoder().encodeToString(cipher.doFinal(password.getBytes(StandardCharsets.UTF_8)));
                        } catch (Exception e) {
                            System.out.println("Error while encrypting: " + e.toString());
                        }

                        try (PreparedStatement st = connection.prepareStatement("INSERT INTO " +
                                "website_data(website, username, password, website_data_id) VALUES (?, ?, ?, ?)")) {
                            st.setString(1, website);
                            st.setString(2, username);
                            st.setString(3, encryptedPass);
                            st.setInt(4, websiteDataId);
                            st.executeUpdate();
                        }

                        System.out.println("The password for website: " + website + " and username: " + user.getUsername() +
                                " has been updated with " + password + ".");
                    }
                    return;
                }
            }
        }

        System.out.print("What is the password?\npassword: ");
        password = input.next();

        try (PreparedStatement stmt = connection.prepareStatement("SELECT MAX(website_data_id) as id FROM website_data")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) websiteDataId = rs.getInt("id") + 1;
        }
        if (websiteDataId == 0) throw new RuntimeException("Error while inserting new password.");

        SecretKey key = User.getKey(user.getDecryptedKey());
        String encryptedPass = "";
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            encryptedPass = Base64.getEncoder().encodeToString(cipher.doFinal(password.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }

        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO " +
                "website_data(website, username, password, website_data_id) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, website);
            stmt.setString(2, username);
            stmt.setString(3, encryptedPass);
            stmt.setInt(4, websiteDataId);
            stmt.executeUpdate();
        }

        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO passwords(user_id, website_data_id) VALUES(?, ?)")) {
            stmt.setInt(1, user.getId());
            stmt.setInt(2, websiteDataId);
            stmt.executeUpdate();
        }

        System.out.println("The website " + website + " with the username " + username +
                " and password " + password + " has been added.");
    }

    private int getWebsiteDataId(String website, String username) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM website_data\n" +
                "LEFT JOIN passwords p on website_data.website_data_id = p.website_data_id\n" +
                "LEFT JOIN users u on p.user_id = u.id\n" +
                "WHERE website like (?)\n" +
                "AND website_data.username like (?)\n" +
                "AND u.id like (?)")) {
            stmt.setString(1, website);
            stmt.setString(2, username);
            stmt.setInt(3, user.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("website_data_id");
        }
        return 0;
    }

    /**
     * Deletes a given website/username combo and its password
     */
    private void deleteWebsite() throws SQLException {
        printAllWebsites();

        System.out.print("Which website?\nwebsite: ");
        String website = input.next();
        System.out.print("Which username?\nusername: ");
        String username = input.next();

        int websiteDataId = getWebsiteDataId(website, username);
        if (websiteDataId == 0) throw new RuntimeException("Error while deleting website.");

        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM passwords " +
                "WHERE user_id like (?) " +
                "AND website_data_id like (?)")) {
            stmt.setInt(1, user.getId());
            stmt.setInt(2, websiteDataId);
            stmt.executeUpdate();
        }

        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM website_data " +
                "WHERE website_data_id like (?)")) {
            stmt.setInt(1, websiteDataId);
            stmt.executeUpdate();
        }

        System.out.println("The website " + website + " was deleted.");
        System.out.println();
    }

    /**
     * Deletes the entire list of websites
     */
    private void deleteUser() throws SQLException {
        System.out.println("Are you sure you want to delete your user? Y/N");
        if (input.next().equalsIgnoreCase("n")) return;

        List<Integer> websiteIds = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM passwords " +
                "WHERE user_id like (?)")) {
            stmt.setInt(1, user.getId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                websiteIds.add(rs.getInt("website_data_id"));
            }
        }

        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM passwords " +
                "WHERE user_id like (?)")) {
            stmt.setInt(1, user.getId());
            stmt.executeUpdate();
        }

        for (int website : websiteIds) {
            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM website_data " +
                    "WHERE website_data_id like (?)")) {
                stmt.setInt(1, website);
                stmt.executeUpdate();
            }
        }

        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM users " +
                "WHERE id like (?)")) {
            stmt.setInt(1, user.getId());
            stmt.executeUpdate();
        }

        System.out.println("User " + user.getUsername() + " deleted.");
        System.exit(0);
    }

    /**
     * Changes the username of a given website and checks if the password needs to be changed
     */
    private void changeUsername() throws SQLException {
        printAllWebsites();

        System.out.print("Which website?\nwebsite: ");
        String website = input.next();

        System.out.println("Do you want to change the password as well? Y/N");
        if (input.next().equalsIgnoreCase("y")) {
            changeWebsite(website);
            return;
        }

        System.out.print("What is the old username?\nusername: ");
        String username = input.next();

        int websiteDataId = getWebsiteDataId(website, username);
        if (websiteDataId == 0) throw new RuntimeException("Error while updating website.");

        System.out.print("What is the new username?\nusername: ");
        String user = input.next();

        try (PreparedStatement stmt = connection.prepareStatement("UPDATE website_data SET username = ? " +
                "WHERE website_data_id like (?)")) {
            stmt.setString(1, user);
            stmt.setInt(2, websiteDataId);
            stmt.executeUpdate();
        }

        System.out.println("Username updated.\n");
    }

    /**
     * Changes the password of a given website and checks if the username needs to changed
     */
    private void changePassword() throws SQLException {
        printAllWebsites();

        System.out.println("Which website?");
        String website = input.next();

        System.out.println("Did the username change as well? Y/N");
        if (input.next().equalsIgnoreCase("y")) {
            changeWebsite(website);
            return;
        }

        System.out.print("What is the username?\nusername: ");
        String username = input.next();

        int websiteDataId = getWebsiteDataId(website, username);
        if (websiteDataId == 0) throw new RuntimeException("Error while updating website.");

        System.out.println("What is the new password?");
        String pass = input.next();

        try (PreparedStatement st = connection.prepareStatement("DELETE FROM website_data " +
                "WHERE website_data_id like (?)")) {
            st.setInt(1, websiteDataId);
            st.executeUpdate();
        }

        SecretKey key = User.getKey(user.getDecryptedKey());
        String encryptedPass = "";
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            encryptedPass = Base64.getEncoder().encodeToString(cipher.doFinal(pass.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }

        try (PreparedStatement st = connection.prepareStatement("INSERT INTO " +
                "website_data(website, username, password, website_data_id) VALUES (?, ?, ?, ?)")) {
            st.setString(1, website);
            st.setString(2, username);
            st.setString(3, encryptedPass);
            st.setInt(4, websiteDataId);
            st.executeUpdate();
        }

        System.out.println("Password updated.");
        System.out.println();
    }

    /**
     * Called when both the username and password need to be updated
     *
     * @param website to be changed
     */
    private void changeWebsite(String website) throws SQLException {
        System.out.println("\nWhat is the old username?\nusername: ");
        String username = input.next();

        int websiteDataId = getWebsiteDataId(website, username);
        if (websiteDataId == 0) throw new RuntimeException("Error while updating website.");

        System.out.println("What is the new username?\nusername: ");
        String user = input.next();
        System.out.println("What is the new password?\npassword: ");
        String pass = input.next();

        try (PreparedStatement st = connection.prepareStatement("DELETE FROM website_data " +
                "WHERE website_data_id like (?)")) {
            st.setInt(1, websiteDataId);
            st.executeUpdate();
        }

        SecretKey key = User.getKey(this.user.getDecryptedKey());
        String encryptedPass = "";
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            encryptedPass = Base64.getEncoder().encodeToString(cipher.doFinal(pass.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }

        try (PreparedStatement st = connection.prepareStatement("INSERT INTO " +
                "website_data(website, username, password, website_data_id) VALUES (?, ?, ?, ?)")) {
            st.setString(1, website);
            st.setString(2, user);
            st.setString(3, encryptedPass);
            st.setInt(4, websiteDataId);
            st.executeUpdate();
        }

        System.out.println(website + " and " + username + " updated with username " + user + " and password " + pass + ".");
        System.out.println();
    }
}