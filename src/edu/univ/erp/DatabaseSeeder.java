package edu.univ.erp;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.data.DatabaseConnector;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseSeeder {

    public static void main(String[] args) {
        System.out.println("🌱 Seeding database...");

        AuthService authService = new AuthService();
        AdminService adminService = new AdminService();
        StudentService studentService = new StudentService(); // Added for enrollment

        // --- 0. Ensure Schema for New Features (Logs & Last Login) ---
        // This ensures your database supports the new features even if you didn't run SQL scripts manually.
        try (Connection conn = DatabaseConnector.getErpConnection()) {
            Statement stmt = conn.createStatement();

            // Create System Logs Table
            String createLogs = "CREATE TABLE IF NOT EXISTS system_logs (" +
                    "log_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id INT, " +
                    "username VARCHAR(50), " +
                    "action VARCHAR(50), " +
                    "details VARCHAR(255), " +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)";
            stmt.executeUpdate(createLogs);
            System.out.println("✅ Schema: 'system_logs' table verified.");

        } catch (Exception e) {
            System.err.println("⚠️ Schema update warning: " + e.getMessage());
        }

        // --- 1. Create the ADMIN User ---
        String adminUser = "admin";
        String adminPass = "admin123";
        String adminHash = BCrypt.hashpw(adminPass, BCrypt.gensalt());

        int adminId = authService.createUser(adminUser, "admin", adminHash);
        if (adminId > 0) System.out.println("✅ Admin created! Login: " + adminUser + " / " + adminPass);
        else System.out.println("⚠️ Admin already exists.");

        // --- 2. Create STUDENT 1 ---
        boolean stu1Success = adminService.createFullStudent("student1", "pass123", "2025001", "CSE", 2025);
        if (stu1Success) System.out.println("✅ Student 1 created! Login: student1 / pass123");
        else System.out.println("⚠️ Student 1 already exists.");

        // --- 3. Create STUDENT 2 ---
        boolean stu2Success = adminService.createFullStudent("student2", "pass123", "2025002", "ECE", 2025);
        if (stu2Success) System.out.println("✅ Student 2 created! Login: student2 / pass123");
        else System.out.println("⚠️ Student 2 already exists.");

        // --- 4. Create INSTRUCTOR ---
        boolean instSuccess = adminService.createFullInstructor("inst1", "pass123", "Computer Science");
        if (instSuccess) System.out.println("✅ Instructor created! Login: inst1 / pass123");
        else System.out.println("⚠️ Instructor already exists.");

        // --- 5. Create COURSES & SECTIONS ---
        System.out.println("📚 Seeding Courses and Sections...");

        try (Connection conn = DatabaseConnector.getErpConnection()) {
            // A. Create Courses
            String sqlCourse = "INSERT IGNORE INTO courses (course_code, title, credits) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlCourse)) {
                ps.setString(1, "CS101"); ps.setString(2, "Intro to Programming"); ps.setInt(3, 4); ps.executeUpdate();
                ps.setString(1, "ECE201"); ps.setString(2, "Digital Circuits"); ps.setInt(3, 4); ps.executeUpdate();
                ps.setString(1, "MATH10"); ps.setString(2, "Calculus I"); ps.setInt(3, 3); ps.executeUpdate();
            }

            // B. Get Instructor ID
            int instId = 0;
            try (PreparedStatement ps = conn.prepareStatement("SELECT user_id FROM auth_db.users_auth WHERE username='inst1'")) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) instId = rs.getInt(1);
            }

            // C. Create Sections
            int sectionIdCS101 = 0;
            if (instId > 0) {
                // We use INSERT IGNORE to prevent errors, but we need the ID for enrollment later
                String sqlSection = "INSERT INTO sections (course_code, instructor_id, day_time, room, capacity, semester, year) VALUES (?, ?, ?, ?, ?, ?, ?)";

                // Check if CS101 section exists first to avoid duplicates in this logic
                boolean exists = false;
                try(PreparedStatement check = conn.prepareStatement("SELECT section_id FROM sections WHERE course_code='CS101' AND instructor_id=?")) {
                    check.setInt(1, instId);
                    ResultSet rs = check.executeQuery();
                    if(rs.next()) {
                        sectionIdCS101 = rs.getInt(1);
                        exists = true;
                    }
                }

                if (!exists) {
                    try (PreparedStatement ps = conn.prepareStatement(sqlSection, Statement.RETURN_GENERATED_KEYS)) {
                        // Section 1: CS101
                        ps.setString(1, "CS101"); ps.setInt(2, instId); ps.setString(3, "Mon/Wed 10:00");
                        ps.setString(4, "L101"); ps.setInt(5, 50); ps.setString(6, "Monsoon"); ps.setInt(7, 2025);
                        ps.executeUpdate();
                        ResultSet rs = ps.getGeneratedKeys();
                        if (rs.next()) sectionIdCS101 = rs.getInt(1);

                        // Section 2: ECE201
                        ps.setString(1, "ECE201"); ps.setInt(2, instId); ps.setString(3, "Tue/Thu 14:00");
                        ps.setString(4, "L102"); ps.setInt(5, 40); ps.setString(6, "Monsoon"); ps.setInt(7, 2025);
                        ps.executeUpdate();

                        System.out.println("✅ Courses and Sections added!");
                    }
                }
            }

            // --- 6. Auto-Enroll Student1 into CS101 (For Testing) ---
            if (sectionIdCS101 > 0) {
                int stu1Id = 0;
                try (PreparedStatement ps = conn.prepareStatement("SELECT user_id FROM auth_db.users_auth WHERE username='student1'")) {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) stu1Id = rs.getInt(1);
                }

                if (stu1Id > 0) {
                    String result = studentService.registerForSection(stu1Id, sectionIdCS101);
                    if (result.startsWith("Success")) {
                        System.out.println("✅ Auto-Enrollment: student1 registered for CS101.");
                    } else {
                        System.out.println("ℹ️ Auto-Enrollment: " + result);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("🌱 Seeding complete.");
    }
}