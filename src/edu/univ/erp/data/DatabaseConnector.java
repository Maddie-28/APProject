package edu.univ.erp.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {

    // --- TiDB Cloud Configuration ---
    private static final String TIDB_HOST = "gateway01.ap-southeast-1.prod.aws.tidbcloud.com";
    private static final String TIDB_USER = "33rqLwfhug4vFMB.root";
    private static final String TIDB_PASS = "gif0vQ3R7uAw9bX8";
    private static final int TIDB_PORT = 4000;

    // SSL Options: strict for Cloud security
    private static final String OPTIONS = "?sslMode=VERIFY_IDENTITY&useSSL=true&enabledTLSProtocols=TLSv1.2,TLSv1.3";

    private static final String AUTH_DB_URL = "jdbc:mysql://" + TIDB_HOST + ":" + TIDB_PORT + "/auth_db" + OPTIONS;
    private static final String ERP_DB_URL =  "jdbc:mysql://" + TIDB_HOST + ":" + TIDB_PORT + "/erp_db" + OPTIONS;

    public static Connection getAuthConnection() throws SQLException {
        return DriverManager.getConnection(AUTH_DB_URL, TIDB_USER, TIDB_PASS);
    }

    public static Connection getErpConnection() throws SQLException {
        return DriverManager.getConnection(ERP_DB_URL, TIDB_USER, TIDB_PASS);
    }
}