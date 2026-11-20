package com.example.postsurgeryrecoverylog;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.SecureRandom;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class AccountDatabase {

    private static final String URL = "jdbc:sqlite:patient_accounts.db";

    public static void initialize() throws SQLException {

        String sql = "CREATE TABLE IF NOT EXISTS accounts (" +
                "username TEXT PRIMARY KEY, " +
                "password_hash TEXT NOT NULL)";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(sql);

            // Create patient accounts
            addAccount(conn, "Phuc", "123");
            addAccount(conn, "Johnathan", "123");
            addAccount(conn, "Pedro", "123");
            addAccount(conn, "Emmanuel", "123");
        }
    }

    // Add an account to the database
    private static void addAccount(Connection conn, String username,
                                   String password) throws SQLException {

        String sql = "INSERT OR IGNORE INTO accounts " +
                "(username, password_hash) VALUES (?, ?)";

        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        String storedHash = Base64.getEncoder().encodeToString(salt) + ":" +
                Base64.getEncoder().encodeToString(hashPassword(password, salt));

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, storedHash);
            stmt.executeUpdate();
        }
    }

    // Check username and password
    public static boolean login(String username, String password)
            throws SQLException {

        String sql = "SELECT password_hash FROM accounts " +
                "WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet result = stmt.executeQuery()) {

                if (result.next()) {
                    String[] parts = result.getString("password_hash").split(":");

                    if (parts.length != 2) {
                        return false;
                    }

                    byte[] salt = Base64.getDecoder().decode(parts[0]);
                    byte[] savedHash = Base64.getDecoder().decode(parts[1]);

                    return MessageDigest.isEqual(
                            savedHash, hashPassword(password, salt));
                }
            }
        }

        return false;
    }

    // Protect stored passwords
    private static byte[] hashPassword(String password, byte[] salt)
            throws SQLException {

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(), salt, 600000, 256);

        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();

        } catch (GeneralSecurityException exception) {
            throw new SQLException("Unable to process password.", exception);

        } finally {
            spec.clearPassword();
        }
    }
}
