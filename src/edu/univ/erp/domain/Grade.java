package edu.univ.erp.domain;

public class Grade{
    private int grade_id;
    private int enrollment_id;
    private String component;
    private double score;

    public Grade() {}

    public Grade(int grade_id, int enrollment_id, String component, double score){
        this.grade_id = grade_id;
        this.enrollment_id = enrollment_id;
        this.component = component;
        this.score = score;
    }

    public int getGrade_id() {
        return grade_id;
    }

    public void setGrade_id(int grade_id) {
        this.grade_id = grade_id;
    }

    public int getEnrollment_id() {
        return enrollment_id;
    }

    public void setEnrollment_id(int enrollment_id) {
        this.enrollment_id = enrollment_id;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Grade{" + "grade_id=" + grade_id + ", enrollment_id=" + enrollment_id + ", component='" + component +
                '\'' + ", score=" + score + '}';
    }
}
