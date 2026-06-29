package edu.univ.erp.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDAO {

    public boolean isDuplicate(int studentId, int sectionId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND section_id = ? AND status != 'dropped'";
        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, sectionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getEnrolledCount(int sectionId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE section_id = ? AND status = 'enrolled'";
        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sectionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean createEnrollment(int studentId, int sectionId) {
        String sql = "INSERT INTO enrollments (student_id, section_id, status) VALUES (?, ?, 'enrolled')";
        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, sectionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) return reactivateEnrollment(studentId, sectionId);
            e.printStackTrace();
            return false;
        }
    }

    private boolean reactivateEnrollment(int studentId, int sectionId) {
        String sql = "UPDATE enrollments SET status = 'enrolled' WHERE student_id = ? AND section_id = ?";
        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, sectionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean dropEnrollment(int studentId, int sectionId) {
        String sql = "UPDATE enrollments SET status = 'dropped' WHERE student_id = ? AND section_id = ? AND status = 'enrolled'";
        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, sectionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Integer> getStudentIdsInSection(int sectionId) {
        List<Integer> studentIds = new ArrayList<>();
        String sql = "SELECT student_id FROM enrollments WHERE section_id = ? AND status = 'enrolled'";
        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sectionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) studentIds.add(rs.getInt("student_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return studentIds;
    }

    public int getEnrollmentId(int studentId, int sectionId) {
        String sql = "SELECT enrollment_id FROM enrollments WHERE student_id = ? AND section_id = ?";
        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, sectionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("enrollment_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<Object[]> getStudentRegistrations(int studentId) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT s.section_id, c.course_code, c.title, s.day_time, s.room, e.status, e.final_grade "
                + "FROM enrollments e "
                + "JOIN sections s ON e.section_id = s.section_id "
                + "JOIN courses c ON s.course_code = c.course_code "
                + "WHERE e.student_id = ? AND e.status != 'dropped'";

        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[] {
                            rs.getInt("section_id"), // Index 0
                            rs.getString("course_code"),
                            rs.getString("title"),
                            rs.getString("day_time"),
                            rs.getString("room"),
                            rs.getString("status"),
                            rs.getString("final_grade")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateFinalGrade(int enrollmentId, String letterGrade) {
        String sql = "UPDATE enrollments SET final_grade = ?, status = 'completed' WHERE enrollment_id = ?";
        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, letterGrade);
            pstmt.setInt(2, enrollmentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getSectionIdByEnrollment(int enrollmentId) {
        String sql = "SELECT section_id FROM enrollments WHERE enrollment_id = ?";
        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, enrollmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("section_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if not found
    }
}