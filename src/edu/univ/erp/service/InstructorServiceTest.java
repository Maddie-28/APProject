package edu.univ.erp.service;

import edu.univ.erp.data.EnrollmentDAO;
import edu.univ.erp.data.GradeDAO;
import edu.univ.erp.data.SectionDAO;
import edu.univ.erp.data.SettingsDAO;
import edu.univ.erp.domain.Grade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class InstructorServiceTest {

    private InstructorService instructorService;

    // Mocks
    private GradeDAO gradeDAO;
    private SettingsDAO settingsDAO;
    private EnrollmentDAO enrollmentDAO;
    private SectionDAO sectionDAO;

    @BeforeEach
    void setUp() {
        gradeDAO = Mockito.mock(GradeDAO.class);
        settingsDAO = Mockito.mock(SettingsDAO.class);
        enrollmentDAO = Mockito.mock(EnrollmentDAO.class);
        sectionDAO = Mockito.mock(SectionDAO.class);

        instructorService = new InstructorService(gradeDAO, settingsDAO, enrollmentDAO, sectionDAO);
    }

    // --- SECURITY TESTS ---

    @Test
    void testEnterScore_WhenInstructorDoesNotTeachSection_ShouldFail() {
        // Arrange
        when(settingsDAO.isMaintenanceModeOn()).thenReturn(false);
        // Enrollment 500 belongs to Section 101
        when(enrollmentDAO.getSectionIdByEnrollment(500)).thenReturn(101);
        // Instructor 99 does NOT teach Section 101
        when(sectionDAO.isInstructorTeachingSection(99, 101)).thenReturn(false);

        // Act
        String result = instructorService.enterScore(99, 500, "Quiz", 85.0);

        // Assert
        assertTrue(result.contains("Security Warning"));
        assertTrue(result.contains("only grade your own sections"));
        verify(gradeDAO, never()).upsertGrade(anyInt(), anyString(), anyDouble());
    }

    @Test
    void testEnterScore_WhenMaintenanceOn_ShouldFail() {
        when(settingsDAO.isMaintenanceModeOn()).thenReturn(true);
        String result = instructorService.enterScore(1, 1, "Quiz", 90);
        assertTrue(result.contains("Maintenance Mode is ON"));
        verify(gradeDAO, never()).upsertGrade(anyInt(), anyString(), anyDouble());
    }

    @Test
    void testEnterScore_Success() {
        // Arrange
        when(settingsDAO.isMaintenanceModeOn()).thenReturn(false);
        when(enrollmentDAO.getSectionIdByEnrollment(500)).thenReturn(101);
        // Instructor 1 teaches Section 101
        when(sectionDAO.isInstructorTeachingSection(1, 101)).thenReturn(true);
        when(gradeDAO.upsertGrade(500, "Quiz", 90)).thenReturn(true);

        // Act
        String result = instructorService.enterScore(1, 500, "Quiz", 90);

        // Assert
        assertTrue(result.startsWith("Success"));
    }

    // --- GRADING CALCULATION TESTS ---

    @Test
    void testComputeFinalGrade_CalculatesCorrectly_B_Grade() {
        // Arrange
        when(settingsDAO.isMaintenanceModeOn()).thenReturn(false);

        // Mock Grades: Quiz(80), Midterm(90), EndSem(80)
        // Calculation: (80*0.2) + (90*0.3) + (80*0.5) = 16 + 27 + 40 = 83 (Grade B)
        List<Grade> mockGrades = Arrays.asList(
                new Grade(1, 500, "Quiz", 80.0),
                new Grade(2, 500, "Midterm", 90.0),
                new Grade(3, 500, "End-Sem", 80.0)
        );
        when(gradeDAO.getGradesForEnrollment(500)).thenReturn(mockGrades);
        when(enrollmentDAO.updateFinalGrade(500, "B")).thenReturn(true);

        // Act
        String result = instructorService.computeAndPublishFinalGrade(500);

        // Assert
        assertTrue(result.contains("B")); // Grade B
        assertTrue(result.contains("83.00")); // Score 83
        verify(enrollmentDAO).updateFinalGrade(500, "B");
    }

    @Test
    void testComputeFinalGrade_CalculatesCorrectly_A_Grade() {
        // Arrange
        when(settingsDAO.isMaintenanceModeOn()).thenReturn(false);

        // Calculation: 100 on everything = 100 (Grade A)
        List<Grade> mockGrades = Arrays.asList(
                new Grade(1, 500, "Quiz", 100.0),
                new Grade(2, 500, "Midterm", 100.0),
                new Grade(3, 500, "End-Sem", 100.0)
        );
        when(gradeDAO.getGradesForEnrollment(500)).thenReturn(mockGrades);
        when(enrollmentDAO.updateFinalGrade(500, "A")).thenReturn(true);

        // Act
        String result = instructorService.computeAndPublishFinalGrade(500);

        // Assert
        assertTrue(result.contains("A"));
    }

    @Test
    void testComputeFinalGrade_NoScores_ShouldError() {
        when(settingsDAO.isMaintenanceModeOn()).thenReturn(false);
        when(gradeDAO.getGradesForEnrollment(500)).thenReturn(Collections.emptyList());

        String result = instructorService.computeAndPublishFinalGrade(500);

        assertTrue(result.contains("No scores found"));
    }
}