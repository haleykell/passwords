package me.haleykell.userdata;

import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;

public class User {
    private String username;
    private String email;
    private String hashedPassword;
    private String encryptedKey;
    private String decryptedKey;
    private int id;

    public User(String username, String email, String hashedPassword, String encryptedKey, int id, String decryptedKey) {
        this.username = username;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.encryptedKey = encryptedKey;
        this.id = id;
        this.decryptedKey = decryptedKey;
    }

    public static User registerNewUser(Connection conn, Scanner input, Pbkdf2PasswordEncoder pwEncoder) throws SQLException {
        System.out.print("Enter your desired username.\nusername: ");
        String username = input.next();
        int id = 0;

        while (true) {
            try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username LIKE (?)")) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) break;
            }
            System.out.print("That username is already taken. Please choose another.\nusername: ");
            username = input.next();
        }

        System.out.print("Enter your desired password.\npassword: ");
        String password = input.next();
        String hashedPassword = pwEncoder.encode(password);

        System.out.print("Please enter your email.\nemail: ");
        String email = input.next();

        try (PreparedStatement stmt = conn.prepareStatement("SELECT MAX(id) as id FROM users")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) id = rs.getInt("id") + 1;
        }

        String alphaNum = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rand = new Random();
        while (salt.length() <= 18) { // length of the random string.
            int index = (int) (rand.nextFloat() * alphaNum.length());
            salt.append(alphaNum.charAt(index));
        }

        String decryptedKey = salt.toString();
        String encryptedKey = "";
        SecretKeySpec secretKey = getKey(password);

        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            encryptedKey = Base64.getEncoder().encodeToString(cipher.doFinal(decryptedKey.getBytes("UTF-8")));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }

        User user = new User(username, email, hashedPassword, encryptedKey, id, decryptedKey);
        user.insertUser(conn);
        return user;
    }

    public static User login(Connection conn, Scanner input, Pbkdf2PasswordEncoder pwEncoder) throws SQLException {
        System.out.print("\nEnter your username and password.\nusername: ");
        String username = input.next();
        System.out.print("password: ");
        String password = input.next();

        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username LIKE (?)")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                while (!pwEncoder.matches(password, rs.getString("master_password_hash"))) {
                    System.out.println("Incorrect password. Please try again.");
                    System.out.print("password: ");
                    password = input.next();
                }
                String hashedPassword = rs.getString("master_password_hash");
                String email = rs.getString("email");
                int id = rs.getInt("id");

                String encryptedKey = rs.getString("password_key");
                String decryptedKey = "";
                SecretKeySpec secretKey = getKey(password);

                try {
                    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
                    cipher.init(Cipher.DECRYPT_MODE, secretKey);
                    decryptedKey = new String(cipher.doFinal(Base64.getDecoder().decode(encryptedKey)));
                } catch (Exception e) {
                    System.out.println("Error while decrypting: " + e.toString());
                }

                return new User(username, email, hashedPassword, encryptedKey, id, decryptedKey);
            }
        }
        return null;
    }

    public static SecretKeySpec getKey(String myKey) {
        MessageDigest sha = null;
        try {
            byte[] key = myKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            return new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getUsername() { return username; }

    public String getEmail() { return email; }

    public String getHashedPassword() { return hashedPassword; }

    public String getEncryptedKey() { return encryptedKey; }

    public String getDecryptedKey() { return decryptedKey; }

    public int getId() { return id; }

    private void insertUser(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO users(id, username, email, master_password_hash, password_key) VALUES(?,?,?,?,?)")) {
            stmt.setInt(1, this.id);
            stmt.setString(2, this.username);
            stmt.setString(3, this.email);
            stmt.setString(4, this.hashedPassword);
            stmt.setString(5, this.encryptedKey);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error inserting new user.");
            throw e;
        }
    }
}
