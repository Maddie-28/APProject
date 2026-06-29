package edu.univ.erp.data;

import edu.univ.erp.domain.Instructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InstructorDAO{

    public Instructor getInstructorProfile(int userId){
        String sql = "SELECT user_id, department FROM instructors WHERE user_id = ?";

        try(Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try(ResultSet rs = pstmt.executeQuery()) {
                if(rs.next()) {
                    Instructor instructor = new Instructor();
                    instructor.setUser_id(rs.getInt("user_id"));
                    instructor.setDepartment(rs.getString("department"));
                    return instructor;
                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createInstructorProfile(int userId, String department) {
        String sql = "INSERT INTO instructors (user_id, department) VALUES (?, ?)";

        try(Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, department);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; // true if 1 row was inserted

        }catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public java.util.List<edu.univ.erp.domain.Instructor> getAllInstructors() {
        java.util.List<edu.univ.erp.domain.Instructor> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM instructors";
        try (java.sql.Connection conn = DatabaseConnector.getErpConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                edu.univ.erp.domain.Instructor i = new edu.univ.erp.domain.Instructor(
                        rs.getInt("user_id"),
                        rs.getString("department")
                );
                list.add(i);
            }
        } catch (java.sql.SQLException e) { e.printStackTrace(); }
        return list;
    }
}