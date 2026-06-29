package edu.univ.erp.data;

import edu.univ.erp.domain.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentDAO{

    public Student getStudentProfile(int userId){
        String sql="SELECT user_id, roll_no, program, year FROM students WHERE user_id = ?";

        try(Connection conn=DatabaseConnector.getErpConnection();
             PreparedStatement pstmt=conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try(ResultSet rs=pstmt.executeQuery()){
                if(rs.next()){
                    return mapRowToStudent(rs);
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    public boolean createStudentProfile(int userId, String rollNo, String program, int year) {
        String sql="INSERT INTO students (user_id, roll_no, program, year) VALUES (?, ?, ?, ?)";

        try(Connection conn=DatabaseConnector.getErpConnection();
             PreparedStatement pstmt=conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, rollNo);
            pstmt.setString(3, program);
            pstmt.setInt(4, year);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; // true if 1 row was inserted

        } catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    private Student mapRowToStudent(ResultSet rs) throws SQLException{
        Student student = new Student();
        student.setUser_id(rs.getInt("user_id"));
        student.setRoll_no(rs.getString("roll_no"));
        student.setProgram(rs.getString("program"));
        student.setYear(rs.getInt("year"));
        return student;
    }


    public java.util.List<edu.univ.erp.domain.Student> getAllStudents() {
        java.util.List<edu.univ.erp.domain.Student> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM students";
        try (java.sql.Connection conn = DatabaseConnector.getErpConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                edu.univ.erp.domain.Student s = new edu.univ.erp.domain.Student(
                        rs.getInt("user_id"),
                        rs.getString("roll_no"),
                        rs.getString("program"),
                        rs.getInt("year")
                );
                list.add(s);
            }
        } catch (java.sql.SQLException e){e.printStackTrace();}
        return list;
    }
}