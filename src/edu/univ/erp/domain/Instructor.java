package edu.univ.erp.domain;

public class Instructor{
    private int user_id;
    private String department;

    public Instructor() {}

    public Instructor(int user_id, String department){
        this.user_id = user_id;
        this.department = department;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return "Instructor{" + "user_id=" + user_id + ", department='" + department + '\'' + '}';
    }
}
