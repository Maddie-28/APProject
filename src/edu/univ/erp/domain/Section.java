package edu.univ.erp.domain;

import java.time.LocalDate;

public class Section{
    private int section_id;
    private String course_code;
    private int instructor_id;
    private String day_time;
    private String room;
    private int capacity;
    private String semester;
    private int year;

    private LocalDate regDeadline;
    private LocalDate dropDeadline;
    private int enrolledCount; // Fetched dynamically, not stored directly

    public Section() {}

    public Section(int section_id, String course_code, int instructor_id, String day_time, String room, int capacity,
                   String semester, int year, LocalDate regDeadline, LocalDate dropDeadline) {
        this.section_id = section_id;
        this.course_code = course_code;
        this.instructor_id = instructor_id;
        this.day_time = day_time;
        this.room = room;
        this.capacity = capacity;
        this.semester = semester;
        this.year = year;
        this.regDeadline = regDeadline;
        this.dropDeadline = dropDeadline;
    }

    // Getters and Setters
    public int getSection_id() { return section_id; }
    public void setSection_id(int section_id) { this.section_id = section_id; }

    public String getCourse_code() { return course_code; }
    public void setCourse_code(String course_code) { this.course_code = course_code; }

    public int getInstructor_id() { return instructor_id; }
    public void setInstructor_id(int instructor_id) { this.instructor_id = instructor_id; }

    public String getDay_time() { return day_time; }
    public void setDay_time(String day_time) { this.day_time = day_time; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public LocalDate getRegDeadline() { return regDeadline; }
    public void setRegDeadline(LocalDate regDeadline) { this.regDeadline = regDeadline; }

    public LocalDate getDropDeadline() { return dropDeadline; }
    public void setDropDeadline(LocalDate dropDeadline) { this.dropDeadline = dropDeadline; }

    public int getEnrolledCount() { return enrolledCount; }
    public void setEnrolledCount(int enrolledCount) { this.enrolledCount = enrolledCount; }

    public int getSeatsLeft() {
        return Math.max(0, capacity - enrolledCount);
    }
}