package edu.univ.erp.domain;

public class Student{
    private int user_id;
    private String roll_no;
    private String program;
    private int year;

    public Student() {}

    public Student(int user_id, String roll_no, String program, int year){
        this.user_id = user_id;
        this.roll_no = roll_no;
        this.program = program;
        this.year = year;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getRoll_no() {
        return roll_no;
    }

    public void setRoll_no(String roll_no) {
        this.roll_no = roll_no;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @Override
    public String toString(){
        return "Student{" + "user_id=" + user_id + ", roll_no='" + roll_no + '\'' + ", program='" + program + '\'' +
                ", year=" + year + '}';
    }
}
