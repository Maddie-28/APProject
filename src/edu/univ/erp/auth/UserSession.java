package edu.univ.erp.auth;

public class UserSession {

    public int userID;
    public String role;
    public String username;
    public String lastLogin;

    public UserSession(int userID, String role, String username, String lastLogin) {
        this.userID = userID;
        this.role = role;
        this.username = username;
        this.lastLogin = lastLogin;
    }
}