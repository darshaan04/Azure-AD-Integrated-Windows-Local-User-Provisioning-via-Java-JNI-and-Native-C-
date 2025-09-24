package com.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

@WebServlet("/registerServlet")
public class RegisterServlet extends HttpServlet 
{
    private UserManager userManager = new UserManager();
    private static final String DB_URL = "jdbc:mysql://localhost:3306/userdb";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";
//    @Override
//    public void init() throws ServletException
//    {
//        super.init();
//        WebhookInitializer.initializeWebhookSubscription();
//    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
    	response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        getServletContext().log("RegisterServlet doPost called");
        System.out.println("RegisterServlet doPost called");
        PrintWriter out = response.getWriter();
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        getServletContext().log("Received username: " + username);
        try {
            AzureCredentials creds = AzureCredentials.getFromDB();
            getServletContext().log("Azure credentials fetched");
            AzureUserManager azureManager = new AzureUserManager(
                    creds.tenantId, creds.clientId, creds.clientSecret);
            boolean azureUserCreated = azureManager.createUser(username, password);
            if (!azureUserCreated) 
            {
                out.println("<h3>Failed to create user in Azure AD.</h3>");
                return;
            }
            getServletContext().log("Azure user created: " + username);
            Class.forName("com.mysql.cj.jdbc.Driver");
            getServletContext().log("MySQL driver loaded");
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) 
            {
                getServletContext().log("DB connection established");
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO users (username, password, role) VALUES (?, ?, ?)"
                );
                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.setString(3, "users");
                int rowsInserted = stmt.executeUpdate();
                getServletContext().log("Inserted rows: " + rowsInserted);
                if (rowsInserted <= 0) {
                    out.println("<h3>Failed to insert user record in DB.</h3>");
                    return;
                }
            }
            userManager.createLocalUser(username, password);
            getServletContext().log("Local user created: " + username);
            out.println("<h3>User registered successfully in Azure AD, DB, and Local Machine!</h3>");
        } 
        catch (Exception e) 
        {
            getServletContext().log("Error in RegisterServlet: " + e.getMessage(), e);
            out.println("<h3>An error occurred while processing your request. Please try again later.</h3>");
        } 
        finally 
        {
            out.close();
        }
    }
}
