package com.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;
import com.microsoft.graph.models.User;

@WebServlet("/graph-webhook")
public class GraphWebhookServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/userdb";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        resp.setContentType("text/plain");
        resp.getWriter().write("GET not supported, please use POST for webhook validation and notifications.");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("Received webhook request at: " + java.time.Instant.now());

        // Log all headers
        System.out.println("----- Request Headers -----");
        for (String headerName : java.util.Collections.list(req.getHeaderNames())) {
            System.out.println(headerName + ": " + req.getHeader(headerName));
        }

        String contentType = req.getContentType();
        int contentLength = req.getContentLength();
        System.out.println("Content-Type: " + contentType);
        System.out.println("Content-Length: " + contentLength);

        String validationTokenParam = req.getParameter("validationToken");
        if (validationTokenParam != null && !validationTokenParam.trim().isEmpty()) {
            System.out.println("Validation token found in query parameter: " + validationTokenParam);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("text/plain");
            resp.getWriter().write(validationTokenParam);
            resp.getWriter().flush();
            return;
        }

        if ("text/plain".equalsIgnoreCase(contentType) && contentLength > 0) {
            String validationTokenBody = req.getReader().lines().collect(Collectors.joining());
            validationTokenBody = validationTokenBody.trim();
            System.out.println("Validation token found in plain text body: " + validationTokenBody);
            if (!validationTokenBody.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("text/plain");
                resp.getWriter().write(validationTokenBody);
                resp.getWriter().flush();
                return;
            } else {
                System.err.println("Empty validation token in plain text body");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        String payload = sb.toString().trim();
        System.out.println("Received JSON payload length: " + payload.length());
        System.out.println("Received JSON payload: " + (payload.isEmpty() ? "<empty>" : payload));

        if (payload.isEmpty()) {
            System.err.println("Empty JSON payload - cannot process");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        JSONObject json;
        try {
            json = new JSONObject(payload);
        } catch (Exception e) {
            System.err.println("Error parsing JSON payload: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String validationTokenJson = null;
        if (json.has("validationToken")) {
            validationTokenJson = json.optString("validationToken", null);
        } else if (json.has("value")) {
            JSONArray values = json.getJSONArray("value");
            if (values.length() > 0) {
                JSONObject firstValue = values.getJSONObject(0);
                validationTokenJson = firstValue.optString("validationToken", null);
            }
        }

        if (validationTokenJson != null && !validationTokenJson.trim().isEmpty()) {
            System.out.println("Validation token found in JSON body: " + validationTokenJson);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("text/plain");
            resp.getWriter().write(validationTokenJson);
            resp.getWriter().flush();
            return;
        }

        JSONArray valueArray = json.getJSONArray("value");

        AzureUserManager azureUserManager;
        try {
            AzureCredentials creds = AzureCredentials.getFromDB();
            azureUserManager = new AzureUserManager(creds.tenantId, creds.clientId, creds.clientSecret);
        } catch (Exception e) {
            System.err.println("AzureUserManager initialization failed: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        GraphHelper graphHelper = new GraphHelper(azureUserManager);

        for (int i = 0; i < valueArray.length(); i++) {
            try {
                JSONObject notification = valueArray.getJSONObject(i);
                JSONObject resourceData = notification.getJSONObject("resourceData");
                String userId = resourceData.getString("id");

                User userDetails = graphHelper.getUserDetailsById(userId);
                String username = userDetails != null ? userDetails.userPrincipalName : "unknown";

                String generatedPassword = azureUserManager.resetUserPassword(userId);
                if (generatedPassword != null) {
                    insertUserToDb(username, generatedPassword);
                    UserManager userManager = new UserManager();
                    userManager.createLocalUser(username, generatedPassword);
                } else {
                    System.err.println("Failed to reset password for user " + username);
                }
            } catch (Exception ex) {
                System.err.println("Error processing notification: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        resp.setStatus(HttpServletResponse.SC_ACCEPTED);
    }
    
    private void insertUserToDb(String username, String password) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO users (username, password, role) VALUES (?, ?, ?)");
                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.setString(3, "users");
                int rows = stmt.executeUpdate();
                System.out.println("Inserted " + rows + " rows for user: " + username);
            }
        } catch (Exception e) {
            System.err.println("Error inserting user " + username);
            e.printStackTrace();
        }
    }
}

