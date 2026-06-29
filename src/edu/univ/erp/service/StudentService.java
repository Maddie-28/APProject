package edu.univ.erp.service;

import edu.univ.erp.data.EnrollmentDAO;
import edu.univ.erp.data.SectionDAO;
import edu.univ.erp.data.SettingsDAO;
import edu.univ.erp.domain.Section;

import java.time.LocalDate;

public class StudentService {

    private final EnrollmentDAO enrollmentDAO;
    private final SectionDAO sectionDAO;
    private final SettingsDAO settingsDAO;

    // 1. DEFAULT CONSTRUCTOR (Used by the Real App)
    public StudentService() {
        this.enrollmentDAO = new EnrollmentDAO();
        this.sectionDAO = new SectionDAO();
        this.settingsDAO = new SettingsDAO();
    }

    // 2. TEST CONSTRUCTOR (Used by Unit Tests to inject Mocks)
    public StudentService(EnrollmentDAO enrollmentDAO, SectionDAO sectionDAO, SettingsDAO settingsDAO) {
        this.enrollmentDAO = enrollmentDAO;
        this.sectionDAO = sectionDAO;
        this.settingsDAO = settingsDAO;
    }

    // --- BUSINESS LOGIC ---

    public String registerForSection(int studentId, int sectionId) {
        // 1. Maintenance Check
        if (settingsDAO.isMaintenanceModeOn()) {
            return "Error: System is in maintenance. Registration is disabled.";
        }

        // 2. Fetch Section details
        Section section = sectionDAO.getSectionById(sectionId);
        if (section == null) {
            return "Error: This section does not exist.";
        }

        // 3. Deadline Check
        if (section.getRegDeadline() != null && LocalDate.now().isAfter(section.getRegDeadline())) {
            return "Error: Registration deadline passed on " + section.getRegDeadline();
        }

        // 4. Duplicate Check
        if (enrollmentDAO.isDuplicate(studentId, sectionId)) {
            return "Error: You are already registered for this section.";
        }

        // 5. Capacity Check
        if (section.getEnrolledCount() >= section.getCapacity()) {
            return "Error: Section is full (" + section.getEnrolledCount() + "/" + section.getCapacity() + ").";
        }

        // 6. Proceed
        boolean success = enrollmentDAO.createEnrollment(studentId, sectionId);
        return success ? "Success! You are registered." : "Error: Database error.";
    }

    public String dropSection(int studentId, int sectionId) {
        // 1. Maintenance Check
        if (settingsDAO.isMaintenanceModeOn()) {
            return "Error: System is in maintenance. Cannot drop sections.";
        }

        // 2. Drop Deadline Check
        Section section = sectionDAO.getSectionById(sectionId);
        if (section != null && section.getDropDeadline() != null) {
            if (LocalDate.now().isAfter(section.getDropDeadline())) {
                return "Error: Drop deadline passed on " + section.getDropDeadline();
            }
        }

        boolean success = enrollmentDAO.dropEnrollment(studentId, sectionId);
        return success ? "Success: Section dropped." : "Error: Could not drop section (or already dropped).";
    }
}