package edu.univ.erp.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingsDAO{

    public boolean isMaintenanceModeOn(){
        String sql = "SELECT setting_value FROM settings WHERE setting_key = 'maintenance_on'";

        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = pstmt.executeQuery()){
                if (rs.next()){
                    return "true".equals(rs.getString("setting_value"));
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public void setMaintenanceMode(boolean isMaintenanceOn){
        String sql="UPDATE settings SET setting_value = ? WHERE setting_key = 'maintenance_on'";

        try(Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, String.valueOf(isMaintenanceOn));
            pstmt.executeUpdate();

        }catch (SQLException e){
            e.printStackTrace();
        }
    }
}