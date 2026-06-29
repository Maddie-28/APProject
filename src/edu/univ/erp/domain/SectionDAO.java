package edu.univ.erp.data;

import edu.univ.erp.domain.Section;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SectionDAO {

    // Helper to map Result Set to Object
    private Section mapRowToSection(ResultSet rs) throws SQLException {
        Section section = new Section();
        section.setSection_id(rs.getInt("section_id"));
        section.setCourse_code(rs.getString("course_code"));
        section.setInstructor_id(rs.getInt("instructor_id"));
        section.setDay_time(rs.getString("day_time"));
        section.setRoom(rs.getString("room"));
        section.setCapacity(rs.getInt("capacity"));
        section.setSemester(rs.getString("semester"));
        section.setYear(rs.getInt("year"));

        // Handle Dates
        Date regDate = rs.getDate("reg_deadline");
        if (regDate != null) section.setRegDeadline(regDate.toLocalDate());

        Date dropDate = rs.getDate("drop_deadline");
        if (dropDate != null) section.setDropDeadline(dropDate.toLocalDate());

        // Handle Enrolled Count (if the query fetched it)
        try {
            section.setEnrolledCount(rs.getInt("enrolled_count"));
        } catch (SQLException e) {
            // Column might not exist in some simple queries, ignore
            section.setEnrolledCount(0);
        }

        return section;
    }

    public Section getSectionById(int sectionId){
        String sql = "SELECT s.*, " +
                "(SELECT COUNT(*) FROM enrollments e WHERE e.section_id = s.section_id AND e.status='enrolled') as enrolled_count " +
                "FROM sections s WHERE section_id = ?";

        try(Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sectionId);
            try(ResultSet rs = pstmt.executeQuery()){
                if (rs.next()) return mapRowToSection(rs);
            }
        }catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Section> getAllAvailableSections(){
        List<Section> list = new ArrayList<>();
        //Gets Section Info + Count of students enrolled
        String sql = "SELECT s.*, " +
                "(SELECT COUNT(*) FROM enrollments e WHERE e.section_id = s.section_id AND e.status='enrolled') as enrolled_count " +
                "FROM sections s";

        try(Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) list.add(mapRowToSection(rs));
        }catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Section> getSectionsByInstructor(int instructorId){
        List<Section> sections = new ArrayList<>();
        String sql = "SELECT * FROM sections WHERE instructor_id = ?";

        try(Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, instructorId);
            try(ResultSet rs = pstmt.executeQuery()) {
                while(rs.next()) sections.add(mapRowToSection(rs));
            }
        }catch (SQLException e) { e.printStackTrace(); }
        return sections;
    }

    public boolean createSection(Section s){
        String sql = "INSERT INTO sections (course_code, instructor_id, day_time, room, capacity, semester, year, reg_deadline, drop_deadline) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try(Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, s.getCourse_code());
            pstmt.setInt(2, s.getInstructor_id());
            pstmt.setString(3, s.getDay_time());
            pstmt.setString(4, s.getRoom());
            pstmt.setInt(5, s.getCapacity());
            pstmt.setString(6, s.getSemester());
            pstmt.setInt(7, s.getYear());

            if(s.getRegDeadline() != null) pstmt.setDate(8, Date.valueOf(s.getRegDeadline()));
            else pstmt.setNull(8, Types.DATE);

            if(s.getDropDeadline() != null) pstmt.setDate(9, Date.valueOf(s.getDropDeadline()));
            else pstmt.setNull(9, Types.DATE);

            return pstmt.executeUpdate() > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteSection(int sectionId){
        String sql = "DELETE FROM sections WHERE section_id = ?";
        try(Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sectionId);
            return pstmt.executeUpdate() > 0;
        }catch (SQLException e) {e.printStackTrace(); return false; }
    }

    public boolean isInstructorTeachingSection(int instructorId, int sectionId){
        String sql = "SELECT COUNT(*) FROM sections WHERE section_id = ? AND instructor_id = ?";
        try(Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sectionId);
            pstmt.setInt(2, instructorId);
            try(ResultSet rs = pstmt.executeQuery()){
                if(rs.next()) return rs.getInt(1) > 0;
            }
        }catch (SQLException e) {e.printStackTrace();}
        return false;
    }
}