package edu.univ.erp.data;

import edu.univ.erp.domain.Course;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

    public List<Course> getAllCourses() {
        String sql = "SELECT course_code, title, credits FROM courses";
        List<Course> courses = new ArrayList<>();

        try (Connection conn = DatabaseConnector.getErpConnection(); // Get connection from ERP DB
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Course course = new Course();
                course.setCourse_code(rs.getString("course_code"));
                course.setTitle(rs.getString("title"));
                course.setCredits(rs.getInt("credits"));

                courses.add(course);
            }
        } catch (SQLException e) {
            System.err.println("Database error fetching courses: " + e.getMessage());
            e.printStackTrace();
        }
        return courses;
    }

    public boolean createCourse(edu.univ.erp.domain.Course course) {
        String sql = "INSERT INTO courses (course_code, title, credits) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, course.getCourse_code());
            pstmt.setString(2, course.getTitle());
            pstmt.setInt(3, course.getCredits());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}