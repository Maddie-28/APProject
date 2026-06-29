package edu.univ.erp.service;

import edu.univ.erp.data.EnrollmentDAO;
import edu.univ.erp.data.GradeDAO;
import edu.univ.erp.data.SectionDAO;
import edu.univ.erp.data.SettingsDAO;
import edu.univ.erp.domain.Grade; // Ensure this import exists

public class InstructorService {
    private final GradeDAO gradeDAO;
    private final SettingsDAO settingsDAO;
    private final EnrollmentDAO enrollmentDAO;
    private final SectionDAO sectionDAO;

    // 1. DEFAULT CONSTRUCTOR (Used by App)
    public InstructorService() {
        this.gradeDAO = new GradeDAO();
        this.settingsDAO = new SettingsDAO();
        this.enrollmentDAO = new EnrollmentDAO();
        this.sectionDAO = new SectionDAO();
    }

    // 2. TEST CONSTRUCTOR (Used by Unit Tests)
    public InstructorService(GradeDAO gradeDAO, SettingsDAO settingsDAO, EnrollmentDAO enrollmentDAO, SectionDAO sectionDAO) {
        this.gradeDAO = gradeDAO;
        this.settingsDAO = settingsDAO;
        this.enrollmentDAO = enrollmentDAO;
        this.sectionDAO = sectionDAO;
    }

    // --- BUSINESS LOGIC ---

    public String enterScore(int instructorId, int enrollmentId, String component, double score) {
        // 1. Check Maintenance Mode
        if (settingsDAO.isMaintenanceModeOn()) {
            return "Error: Maintenance Mode is ON. Grades cannot be changed.";
        }

        // 2. Security Check: Ensure instructor teaches this section
        int sectionId = enrollmentDAO.getSectionIdByEnrollment(enrollmentId);
        if (sectionId == -1) {
            return "Error: Enrollment not found.";
        }

        if (!sectionDAO.isInstructorTeachingSection(instructorId, sectionId)) {
            return "Error: Security Warning! You can only grade your own sections.";
        }

        // 3. Save Grade
        boolean success = gradeDAO.upsertGrade(enrollmentId, component, score);
        return success ? "Success: Score saved." : "Error: Could not save score.";
    }

    public String computeAndPublishFinalGrade(int enrollmentId) {
        if (settingsDAO.isMaintenanceModeOn()) {
            return "Error: Maintenance Mode is ON.";
        }

        java.util.List<Grade> grades = gradeDAO.getGradesForEnrollment(enrollmentId);
        if (grades.isEmpty()) return "Error: No scores found for this student.";

        double totalScore = 0.0;
        for (Grade g : grades) {
            String type = g.getComponent();
            double score = g.getScore();
            switch (type) {
                case "Quiz": totalScore += score * 0.20; break;
                case "Midterm": totalScore += score * 0.30; break;
                case "End-Sem": totalScore += score * 0.50; break;
            }
        }

        String letterGrade;
        if (totalScore >= 90) letterGrade = "A";
        else if (totalScore >= 80) letterGrade = "B";
        else if (totalScore >= 70) letterGrade = "C";
        else if (totalScore >= 60) letterGrade = "D";
        else letterGrade = "F";

        boolean success = enrollmentDAO.updateFinalGrade(enrollmentId, letterGrade);
        if (success) return "Success: Final Grade Calculated: " + letterGrade + " (" + String.format("%.2f", totalScore) + "%)";
        else return "Error: Database update failed.";
    }

    public String generateSectionStatistics(int sectionId) {
        // (This is a read-only report, so usually no maintenance check needed, but added for safety if desired)
        StringBuilder report = new StringBuilder();
        String[] components = {"Quiz", "Midterm", "End-Sem"};
        report.append("--- Class Statistics ---\n");

        for (String comp : components) {
            java.util.List<Double> scores = gradeDAO.getScoresBySection(sectionId, comp);
            if (scores.isEmpty()) {
                report.append(String.format("%-12s: No data yet\n", comp));
                continue;
            }
            double sum = 0;
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            for (double s : scores) {
                sum += s;
                if (s < min) min = s;
                if (s > max) max = s;
            }
            report.append(String.format("%-12s: Avg: %5.2f | Min: %5.2f | Max: %5.2f\n", comp, sum / scores.size(), min, max));
        }
        return report.toString();
    }
}