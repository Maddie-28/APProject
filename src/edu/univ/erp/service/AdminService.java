package edu.univ.erp.service;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.data.StudentDAO;
import edu.univ.erp.data.InstructorDAO;
import edu.univ.erp.data.SettingsDAO;
import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.data.SectionDAO;
import edu.univ.erp.data.EnrollmentDAO;
import edu.univ.erp.domain.*;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AdminService {

    private final AuthService authService;
    private final StudentDAO studentDAO;
    private final InstructorDAO instructorDAO;
    private final SettingsDAO settingsDAO;
    private final CourseDAO courseDAO;
    private final SectionDAO sectionDAO;
    private final EnrollmentDAO enrollmentDAO;

    // --- TiDB Cloud Credentials ---
    private static final String DB_HOST = "gateway01.ap-southeast-1.prod.aws.tidbcloud.com";
    private static final String DB_PORT = "4000";
    private static final String DB_USER = "33rqLwfhug4vFMB.root";
    private static final String DB_PASS = "gif0vQ3R7uAw9bX8";
    private static final String DB_NAME = "erp_db";

    public AdminService() {
        this.authService = new AuthService();
        this.studentDAO = new StudentDAO();
        this.instructorDAO = new InstructorDAO();
        this.settingsDAO = new SettingsDAO();
        this.courseDAO = new CourseDAO();
        this.sectionDAO = new SectionDAO();
        this.enrollmentDAO = new EnrollmentDAO();
    }

    // Constructor for Tests
    public AdminService(AuthService authService, StudentDAO studentDAO, InstructorDAO instructorDAO,
                        SettingsDAO settingsDAO, CourseDAO courseDAO, SectionDAO sectionDAO, EnrollmentDAO enrollmentDAO) {
        this.authService = authService;
        this.studentDAO = studentDAO;
        this.instructorDAO = instructorDAO;
        this.settingsDAO = settingsDAO;
        this.courseDAO = courseDAO;
        this.sectionDAO = sectionDAO;
        this.enrollmentDAO = enrollmentDAO;
    }

    // --- SECTION MANAGEMENT ---
    public boolean createSection(Section s) {
        // Validation Logic
        if (s.getCapacity() <= 0) return false;
        if (s.getCourse_code() == null || s.getCourse_code().isEmpty()) return false;

        // Pass to DAO
        return sectionDAO.createSection(s);
    }

    public String deleteSection(int sectionId) {
        // Safety Check: Are students enrolled?
        int enrolledCount = enrollmentDAO.getEnrolledCount(sectionId);

        if (enrolledCount > 0) {
            return "Error: Cannot delete section. " + enrolledCount + " student(s) are still enrolled.";
        }

        boolean success = sectionDAO.deleteSection(sectionId);
        return success ? "Success: Section deleted." : "Error: Database failed to delete section.";
    }

    // --- USER CREATION ---
    public boolean createFullStudent(String username, String password, String rollNo, String program, int year) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        int newUserId = authService.createUser(username, "student", hashedPassword);
        if (newUserId <= 0) return false;
        return studentDAO.createStudentProfile(newUserId, rollNo, program, year);
    }

    public boolean createFullInstructor(String username, String password, String department) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        int newUserId = authService.createUser(username, "instructor", hashedPassword);
        if (newUserId <= 0) return false;
        return instructorDAO.createInstructorProfile(newUserId, department);
    }

    // --- COURSE MANAGEMENT ---
    public boolean createCourse(String code, String title, int credits) {
        Course c = new Course(code, title, credits);
        return courseDAO.createCourse(c);
    }

    // --- FETCH DATA ---
    public List<Instructor> getAllInstructors() { return instructorDAO.getAllInstructors(); }
    public List<Student> getAllStudents() { return studentDAO.getAllStudents(); }
    public List<Course> getAllCourses() { return courseDAO.getAllCourses(); }
    public List<Section> getAllSections() { return sectionDAO.getAllAvailableSections(); }

    // --- MAINTENANCE ---
    public void toggleMaintenanceMode(boolean isMaintenanceOn) {
        settingsDAO.setMaintenanceMode(isMaintenanceOn);
    }

    // --- BACKUP & RESTORE ---
    public boolean backupDatabase(File destinationFile) {
        List<String> command = new ArrayList<>();
        command.add("mysqldump");
        command.add("-h"); command.add(DB_HOST);
        command.add("-P"); command.add(DB_PORT);
        command.add("-u"); command.add(DB_USER);
        command.add("-p" + DB_PASS);
        command.add("--ssl-mode=REQUIRED"); // Critical for TiDB
        command.add("--databases"); command.add(DB_NAME);
        command.add("-r"); command.add(destinationFile.getAbsolutePath());

        ProcessBuilder pb = new ProcessBuilder(command);
        try {
            Process process = pb.start();
            return process.waitFor() == 0;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean restoreDatabase(File sourceFile) {
        // Note: Restoring to cloud requires 'mysql' client in system PATH
        String importCmd = String.format("mysql -h%s -P%s -u%s -p%s --ssl-mode=REQUIRED %s < \"%s\"",
                DB_HOST, DB_PORT, DB_USER, DB_PASS, DB_NAME, sourceFile.getAbsolutePath());

        ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", importCmd);
        try {
            Process process = pb.start();
            return process.waitFor() == 0;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
}