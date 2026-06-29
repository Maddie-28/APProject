package edu.univ.erp.service;

import edu.univ.erp.data.EnrollmentDAO;
import edu.univ.erp.data.SectionDAO;
import edu.univ.erp.data.SettingsDAO;
import edu.univ.erp.domain.Section;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class StudentServiceTest {

    private StudentService studentService;

    // Mocks
    private EnrollmentDAO enrollmentDAO;
    private SectionDAO sectionDAO;
    private SettingsDAO settingsDAO;



    @BeforeEach
    void setUp() {
        enrollmentDAO = Mockito.mock(EnrollmentDAO.class);
        sectionDAO = Mockito.mock(SectionDAO.class);
        settingsDAO = Mockito.mock(SettingsDAO.class);

        studentService = new StudentService(enrollmentDAO, sectionDAO, settingsDAO);
    }

    // --- REGISTRATION TESTS ---

    @Test
    void testRegister_WhenMaintenanceOn_ShouldFail() {
        // Arrange
        when(settingsDAO.isMaintenanceModeOn()).thenReturn(true);

        // Act
        String result = studentService.registerForSection(1, 101);

        // Assert
        assertTrue(result.contains("maintenance"));
        verify(enrollmentDAO, never()).createEnrollment(anyInt(), anyInt());
    }

    @Test
    void testRegister_WhenDeadlinePassed_ShouldFail() {
        // Arrange
        when(settingsDAO.isMaintenanceModeOn()).thenReturn(false);

        Section mockSection = new Section();
        mockSection.setSection_id(101);
        // Set deadline to yesterday
        mockSection.setRegDeadline(LocalDate.now().minusDays(1));

        when(sectionDAO.getSectionById(101)).thenReturn(mockSection);

        // Act
        String result = studentService.registerForSection(1, 101);

        // Assert
        assertTrue(result.contains("deadline passed"));
        verify(enrollmentDAO, never()).createEnrollment(anyInt(), anyInt());
    }

    @Test
    void testRegister_WhenAlreadyRegistered_ShouldFail() {
        // Arrange
        when(settingsDAO.isMaintenanceModeOn()).thenReturn(false);

        Section mockSection = new Section();
        mockSection.setRegDeadline(LocalDate.now().plusDays(5)); // Valid deadline
        when(sectionDAO.getSectionById(101)).thenReturn(mockSection);

        // Mock Duplicate check
        when(enrollmentDAO.isDuplicate(1, 101)).thenReturn(true);

        // Act
        String result = studentService.registerForSection(1, 101);

        // Assert
        assertTrue(result.contains("already registered"));
    }

    @Test
    void testRegister_WhenSectionFull_ShouldFail() {
        // Arrange
        when(settingsDAO.isMaintenanceModeOn()).thenReturn(false);
        when(enrollmentDAO.isDuplicate(1, 101)).thenReturn(false);

        // Mock a FULL section
        Section mockSection = new Section();
        mockSection.setCapacity(30);
        mockSection.setEnrolledCount(30); // 30/30 filled
        mockSection.setRegDeadline(LocalDate.now().plusDays(5)); // Valid deadline

        when(sectionDAO.getSectionById(101)).thenReturn(mockSection);

        // Act
        String result = studentService.registerForSection(1, 101);

        // Assert
        assertTrue(result.contains("Section is full"));
        verify(enrollmentDAO, never()).createEnrollment(anyInt(), anyInt());
    }

    @Test
    void testRegister_Success() {
        // Arrange
        when(settingsDAO.isMaintenanceModeOn()).thenReturn(false);
        when(enrollmentDAO.isDuplicate(1, 101)).thenReturn(false);

        // Mock a Valid section with space
        Section mockSection = new Section();
        mockSection.setCapacity(30);
        mockSection.setEnrolledCount(5); // 5/30 filled
        mockSection.setRegDeadline(LocalDate.now().plusDays(5)); // Valid deadline

        when(sectionDAO.getSectionById(101)).thenReturn(mockSection);
        when(enrollmentDAO.createEnrollment(1, 101)).thenReturn(true);

        // Act
        String result = studentService.registerForSection(1, 101);

        // Assert
        assertTrue(result.startsWith("Success"));
    }

    // --- DROP TESTS ---

    @Test
    void testDrop_WhenDropDeadlinePassed_ShouldFail() {
        // Arrange
        when(settingsDAO.isMaintenanceModeOn()).thenReturn(false);

        Section mockSection = new Section();
        // Set drop deadline to yesterday
        mockSection.setDropDeadline(LocalDate.now().minusDays(1));

        when(sectionDAO.getSectionById(101)).thenReturn(mockSection);

        // Act
        String result = studentService.dropSection(1, 101);

        // Assert
        assertTrue(result.contains("Drop deadline passed"));
        verify(enrollmentDAO, never()).dropEnrollment(anyInt(), anyInt());
    }

    @Test
    void testDrop_Success() {
        // Arrange
        when(settingsDAO.isMaintenanceModeOn()).thenReturn(false);

        Section mockSection = new Section();
        // Valid deadline (tomorrow)
        mockSection.setDropDeadline(LocalDate.now().plusDays(1));
        when(sectionDAO.getSectionById(101)).thenReturn(mockSection);
        when(enrollmentDAO.dropEnrollment(1, 101)).thenReturn(true);

        // Act
        String result = studentService.dropSection(1, 101);

        // Assert
        assertTrue(result.startsWith("Success"));
    }
}