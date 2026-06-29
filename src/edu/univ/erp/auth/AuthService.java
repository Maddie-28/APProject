package edu.univ.erp.auth;

import edu.univ.erp.data.DatabaseConnector;
import edu.univ.erp.data.LoggerDAO;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class AuthService {

    private final LoggerDAO loggerDAO = new LoggerDAO();

    public UserSession login(String username, String plainPassword) throws Exception {
        String sql = "SELECT user_id, password_hash, role, status, last_login, failed_attempts, lockout_until, NOW() as db_time FROM users_auth WHERE username = ?";

        try (Connection conn = DatabaseConnector.getAuthConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()){
                if (rs.next()){
                    int userId = rs.getInt("user_id");
                    String storedHash = rs.getString("password_hash");
                    String role = rs.getString("role");
                    String status = rs.getString("status");
                    String lastLoginDB = rs.getString("last_login");
                    int failedAttempts = rs.getInt("failed_attempts");

                    Timestamp lockoutUntil = rs.getTimestamp("lockout_until");
                    Timestamp currentDbTime = rs.getTimestamp("db_time"); // Get DB server time

                    // 1. CHECK LOCKOUT STATUS (Comparing DB time vs DB time)
                    if (lockoutUntil != null && lockoutUntil.after(currentDbTime)) {
                        long diffMillis = lockoutUntil.getTime() - currentDbTime.getTime();
                        long diffMinutes = (diffMillis / (60 * 1000)) + 1; // Round up

                        loggerDAO.log(userId, username, "LOGIN_BLOCKED", "Attempted login while locked");
                        throw new Exception("Account locked! Try again in " + diffMinutes + " minutes.");
                    }

                    // 2. CHECK ACCOUNT STATUS
                    if (!"active".equalsIgnoreCase(status)) {
                        throw new Exception("Account is inactive or suspended.");
                    }

                    // 3. VERIFY PASSWORD
                    if (BCrypt.checkpw(plainPassword, storedHash)) {
                        // SUCCESS: Reset counters
                        resetLockout(userId);

                        loggerDAO.log(userId, username, "LOGIN_SUCCESS", "User logged in");
                        updateLastLogin(userId);

                        String displayTime = (lastLoginDB == null) ? "First Login" : lastLoginDB;
                        return new UserSession(userId, role, username, displayTime);
                    } else {
                        // FAILURE: Increment counters
                        handleFailedLogin(userId, username, failedAttempts);
                        throw new Exception("Incorrect password.");
                    }
                } else {
                    // User not found
                    loggerDAO.log(0, username, "LOGIN_FAILED", "User not found");
                    throw new Exception("User not found.");
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
            throw new Exception("Database Connection Error.");
        }
    }

    private void handleFailedLogin(int userId, String username, int currentAttempts) {
        int newAttempts = currentAttempts + 1;
        String sql;

        // If 5th failure (or more), set lockout for 5 minutes
        if (newAttempts >= 5) {
            sql = "UPDATE users_auth SET failed_attempts = ?, lockout_until = DATE_ADD(NOW(), INTERVAL 5 MINUTE) WHERE user_id = ?";
            loggerDAO.log(userId, username, "ACCOUNT_LOCKED", "5 failed attempts. Locked for 5 min.");
        } else {
            sql = "UPDATE users_auth SET failed_attempts = ? WHERE user_id = ?";
            loggerDAO.log(userId, username, "LOGIN_FAILED", "Incorrect password. Attempt " + newAttempts + "/5");
        }

        try (Connection conn = DatabaseConnector.getAuthConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newAttempts);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void resetLockout(int userId) {
        String sql = "UPDATE users_auth SET failed_attempts = 0, lockout_until = NULL WHERE user_id = ?";
        try (Connection conn = DatabaseConnector.getAuthConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void updateLastLogin(int userId) {
        String sql = "UPDATE users_auth SET last_login = NOW() WHERE user_id = ?";
        try (Connection conn = DatabaseConnector.getAuthConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public int createUser(String username, String role, String hashedPassword){
        String sql = "INSERT INTO users_auth (username, role, password_hash) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnector.getAuthConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)){
            pstmt.setString(1, username);
            pstmt.setString(2, role);
            pstmt.setString(3, hashedPassword);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0){
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()){
                    if (generatedKeys.next()){
                        int newId = generatedKeys.getInt(1);
                        loggerDAO.log(0, "System", "CREATE_USER", "Created new " + role + ": " + username);
                        return newId;
                    }
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
            loggerDAO.log(0, "System", "CREATE_FAILED", "Failed to create user: " + username);
        }
        return -1;
    }

    public boolean updatePassword(int userId, String oldPassword, String newPassword) {
        String sqlGet = "SELECT password_hash, username FROM users_auth WHERE user_id = ?";
        String sqlUpdate = "UPDATE users_auth SET password_hash = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnector.getAuthConnection()) {
            String currentHash = null;
            String username = "Unknown";
            try (PreparedStatement ps = conn.prepareStatement(sqlGet)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        currentHash = rs.getString("password_hash");
                        username = rs.getString("username");
                    }
                }
            }
            if (currentHash == null) return false;
            if (!BCrypt.checkpw(oldPassword, currentHash)) {
                loggerDAO.log(userId, username, "PASS_UPDATE_FAIL", "Incorrect old password");
                return false;
            }
            String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setString(1, newHash);
                ps.setInt(2, userId);
                boolean success = ps.executeUpdate() > 0;
                if(success) loggerDAO.log(userId, username, "PASS_UPDATE_SUCCESS", "Password changed successfully");
                return success;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}