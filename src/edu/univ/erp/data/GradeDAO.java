package edu.univ.erp.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GradeDAO {

    // Insert or Update a grade (Upsert)
    public boolean upsertGrade(int enrollmentId, String component, double score) {
        // This query checks if a grade exists; if yes, updates it; if no, inserts it.
        // Note: This requires a UNIQUE constraint on (enrollment_id, component) in DB,
        // OR you can use a simple logic: Delete old -> Insert new.
        // For simplicity in this project, we will just INSERT.
        // In a real app, check if exists first.

        String sql = "INSERT INTO grades (enrollment_id, component, score) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnector.getErpConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, enrollmentId);
            pstmt.setString(2, component);
            pstmt.setDouble(3, score);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //Fetch all grades for a specific enrollment
    public java.util.List<edu.univ.erp.domain.Grade> getGradesForEnrollment(int enrollmentId) {
        java.util.List<edu.univ.erp.domain.Grade> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM grades WHERE enrollment_id = ?";

        try (Connection conn = DatabaseConnector.getErpConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, enrollmentId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    edu.univ.erp.domain.Grade g =
                            new edu.univ.erp.domain.Grade(
                                    rs.getInt("grade_id"),
                                    rs.getInt("enrollment_id"),
                                    rs.getString("component"),
                                    rs.getDouble("score"));
                    list.add(g);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    //Fetch all scores for a specific section and component(e.g., all 'Quiz' scores for CS101)
    public java.util.List<Double> getScoresBySection(int sectionId, String component) {
        java.util.List<Double> scores = new java.util.ArrayList<>();
        String sql =
                "SELECT g.score FROM grades g "
                        + "JOIN enrollments e ON g.enrollment_id = e.enrollment_id "
                        + "WHERE e.section_id = ? AND g.component = ?";

        try (Connection conn = DatabaseConnector.getErpConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sectionId);
            pstmt.setString(2, component);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    scores.add(rs.getDouble("score"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return scores;
    }
}