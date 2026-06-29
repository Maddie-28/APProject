package edu.univ.erp.domain;

public class Course {

    private String course_code;
    private String title;
    private int credits;

    public Course() {}

    public Course(String course_code, String title, int credits) {
        this.course_code = course_code;
        this.title = title;
        this.credits = credits;
    }

    public String getCourse_code() {
        return this.course_code;
    }

    public void setCourse_code(String course_code) {
        this.course_code = course_code;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getCredits() {
        return this.credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    @Override
    public String toString() {
        return "Course{" +
                "course_code='" + course_code + '\'' +
                ", title='" + title + '\'' +
                ", credits='" + credits + '\'' +
                '}';
    }
}