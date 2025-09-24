package com.example;
import java.sql.*;

public class AzureCredentials {
    public String tenantId;
    public String clientId;
    public String clientSecret;
    public static AzureCredentials getFromDB() throws SQLException {
        AzureCredentials creds = new AzureCredentials();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL Driver class not found", e);
        }
        Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/azure_config", "root", "root");
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT tenant_id, client_id, client_secret FROM azure_credentials LIMIT 1");
        if (rs.next())
        {
            creds.tenantId = rs.getString("tenant_id");
            creds.clientId = rs.getString("client_id");
            creds.clientSecret = rs.getString("client_secret");
        }
        rs.close();
        stmt.close();
        conn.close();
        return creds;
    }
}
