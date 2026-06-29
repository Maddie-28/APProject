package edu.univ.erp.service;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.data.StudentDAO;
import edu.univ.erp.data.InstructorDAO;
import edu.univ.erp.data.SettingsDAO;
import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.data.SectionDAO;
import edu.univ.erp.data.EnrollmentDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdminServiceTest {

    private AdminService adminService;

    // Mocks for all dependencies
    private AuthService authService;
    private StudentDAO studentDAO;
    private InstructorDAO instructorDAO;
    private SettingsDAO settingsDAO;
    private CourseDAO courseDAO;
    private SectionDAO sectionDAO;
    private EnrollmentDAO enrollmentDAO;

    @BeforeEach
    void setUp() {
        // 1. Create Mocks (Fake objects)
        authService = Mockito.mock(AuthService.class);
        studentDAO = Mockito.mock(StudentDAO.class);
        instructorDAO = Mockito.mock(InstructorDAO.class);
        settingsDAO = Mockito.mock(SettingsDAO.class);
        courseDAO = Mockito.mock(CourseDAO.class);
        sectionDAO = Mockito.mock(SectionDAO.class);
        enrollmentDAO = Mockito.mock(EnrollmentDAO.class);

        // 2. Inject them into AdminService using the Test Constructor
        adminService = new AdminService(
                authService,
                studentDAO,
                instructorDAO,
                settingsDAO,
                courseDAO,
                sectionDAO,
                enrollmentDAO
        );
    }

    // --- STUDENT CREATION TESTS ---

    @Test
    void testCreateStudent_Success() {
        // Arrange: Auth creates user ID 55, and Profile creation succeeds
        when(authService.createUser(anyString(), eq("student"), anyString())).thenReturn(55);
        when(studentDAO.createStudentProfile(eq(55), anyString(), anyString(), anyInt())).thenReturn(true);

        // Act
        boolean result = adminService.createFullStudent("john", "pass", "Roll1", "CSE", 2025);

        // Assert
        assertTrue(result);
        verify(authService).createUser(eq("john"), eq("student"), anyString());
        verify(studentDAO).createStudentProfile(eq(55), eq("Roll1"), eq("CSE"), eq(2025));
    }

    @Test
    void testCreateStudent_Fail_IfAuthFails() {
        // Arrange: Auth fails (returns -1)
        when(authService.createUser(anyString(), anyString(), anyString())).thenReturn(-1);

        // Act
        boolean result = adminService.createFullStudent("john", "pass", "Roll1", "CSE", 2025);

        // Assert
        assertFalse(result);
        // Verify we never tried to create a profile for a non-existent user
        verify(studentDAO, never()).createStudentProfile(anyInt(), anyString(), anyString(), anyInt());
    }

    // --- SECTION DELETION TESTS ---

    @Test
    void testDeleteSection_WhenStudentsEnrolled_ShouldFail() {
        // Arrange: Mock that 5 students are enrolled in section 101
        when(enrollmentDAO.getEnrolledCount(101)).thenReturn(5);

        // Act
        String result = adminService.deleteSection(101);

        // Assert
        assertTrue(result.contains("Cannot delete"));
        assertTrue(result.contains("student(s) are still enrolled"));

        // Critical: Verify we NEVER called the delete method on the DAO
        verify(sectionDAO, never()).deleteSection(101);
    }

    @Test
    void testDeleteSection_WhenEmpty_ShouldSuccess() {
        // Arrange: Mock that 0 students are enrolled
        when(enrollmentDAO.getEnrolledCount(102)).thenReturn(0);
        when(sectionDAO.deleteSection(102)).thenReturn(true);

        // Act
        String result = adminService.deleteSection(102);

        // Assert
        assertTrue(result.startsWith("Success"));
        // Verify we actually called delete this time
        verify(sectionDAO).deleteSection(102);
    }
}