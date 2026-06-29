package edu.univ.erp.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LoggerDAO {

    // Log an action (Fire and forget)
    public void log(int userId, String username, String action, String details) {
        String sql =
                "INSERT INTO system_logs (user_id, username, action, details) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnector.getErpConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, username);
            pstmt.setString(3, action);
            pstmt.setString(4, details);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Fetch logs for Admin Dashboard
    public List<Object[]> getAllLogs() {
        List<Object[]> logs = new ArrayList<>();
        String sql =
                "SELECT log_id, username, action, details, timestamp FROM system_logs ORDER BY timestamp DESC LIMIT 100";

        try (Connection conn = DatabaseConnector.getErpConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                logs.add(
                        new Object[] {
                            rs.getInt("log_id"),
                            rs.getString("timestamp"),
                            rs.getString("username"),
                            rs.getString("action"),
                            rs.getString("details")
                        });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
}