package com.example;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.http.GraphServiceException;
import com.microsoft.graph.models.PasswordProfile;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.GraphServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.regex.Pattern;

public class AzureUserManager {
    private static final Logger logger = LoggerFactory.getLogger(AzureUserManager.class);
    private final GraphServiceClient<?> graphClient;
    private static final String VERIFIED_DOMAIN = "arunachermaduraiml.onmicrosoft.com";

    public AzureUserManager(String tenantId, String clientId, String clientSecret) 
    {
        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tenantId(tenantId)
                .build();
        TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(
                Arrays.asList("https://graph.microsoft.com/.default"),
                credential);
        graphClient = GraphServiceClient.builder()
                .authenticationProvider(authProvider)
                .buildClient();
    }

    private static final Pattern MAIL_NICKNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");

    private String sanitizeMailNickname(String username) 
    {
        String sanitized = username.replaceAll("[^a-zA-Z0-9_-]", "");
        if (sanitized.length() > 64)
            sanitized = sanitized.substring(0, 64);
        return sanitized;
    }

    private boolean isValidPassword(String password) 
    {
        return password != null && password.length() >= 8;
    }

    private boolean userExists(String mailNickname) 
    {
        try {
            var users = graphClient.users()
                    .buildRequest()
                    .filter("mailNickname eq '" + mailNickname + "'")
                    .get();
            return users.getCurrentPage().size() > 0;
        } catch (Exception e) {
            logger.error("Error checking existing user", e);
            return false;
        }
    }
    public boolean createUser(String username, String password) throws Exception {
        if (!isValidPassword(password))
            throw new IllegalArgumentException("Password does not meet minimum requirements");
        String mailNickname = sanitizeMailNickname(username);
        if (mailNickname.isEmpty() || !MAIL_NICKNAME_PATTERN.matcher(mailNickname).matches())
            throw new IllegalArgumentException("Invalid sanitized mailNickname: " + mailNickname);
        if (userExists(mailNickname))
            throw new IllegalArgumentException("User with mailNickname already exists: " + mailNickname);
        try {
            User user = new User();
            user.accountEnabled = true;
            user.displayName = username;
            user.mailNickname = mailNickname;
            user.userPrincipalName = mailNickname + "@" + VERIFIED_DOMAIN;
            PasswordProfile passwordProfile = new PasswordProfile();
            passwordProfile.password = password;
            passwordProfile.forceChangePasswordNextSignIn = false;
            user.passwordProfile = passwordProfile;
            graphClient.users()
                    .buildRequest()
                    .post(user);
            logger.info("Azure user created successfully: {}", username);
            return true;
        } catch (GraphServiceException e) {
            logger.error("Microsoft Graph API error creating user: {}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error creating user", e);
            return false;
        }
    }

    public String resetUserPassword(String userId) {
        try {
            String newPassword = PasswordUtil.generateSecurePassword(12);
            User user = new User();
            PasswordProfile passwordProfile = new PasswordProfile();
            passwordProfile.password = newPassword;
            passwordProfile.forceChangePasswordNextSignIn = false;
            user.passwordProfile = passwordProfile;

            graphClient.users(userId)
                    .buildRequest()
                    .patch(user);

            logger.info("Password reset successfully for user ID: {}", userId);
            return newPassword;
        } catch (Exception e) {
            logger.error("Error resetting password for user ID {}: {}", userId, e.getMessage(), e);
            return null;
        }
    }

    public GraphServiceClient<?> getGraphClient() {
        return graphClient;
    }
}
