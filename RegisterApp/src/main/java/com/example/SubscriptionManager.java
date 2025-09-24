package com.example;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.Subscription;
import com.microsoft.graph.requests.GraphServiceClient;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;

public class SubscriptionManager 
{
    private GraphServiceClient<?> graphClient;
    public SubscriptionManager(String tenantId, String clientId, String clientSecret) 
    {
        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                .tenantId(tenantId)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
        TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(
                Arrays.asList("https://graph.microsoft.com/.default"), credential);
        graphClient = GraphServiceClient.builder().authenticationProvider(authProvider).buildClient();
    }
    public Subscription createOrUpdateSubscription(String notificationUrl) 
    {
        Subscription subscription = new Subscription();
        subscription.changeType = "created";
        subscription.notificationUrl = notificationUrl; 
        subscription.resource = "/users";
        subscription.expirationDateTime = OffsetDateTime.now(ZoneOffset.UTC).plusDays(3);
        subscription.clientState = "YourClientState123";
        System.out.println("Subscription details to create:");
        System.out.println("Change Type: " + subscription.changeType);
        System.out.println("Notification URL: " + subscription.notificationUrl);
        System.out.println("Resource: " + subscription.resource);
        System.out.println("Expiration Time: " + subscription.expirationDateTime);
        System.out.println("Client State: " + subscription.clientState);
        try 
        {
            Subscription createdSubscription = graphClient.subscriptions()
                    .buildRequest()
                    .post(subscription);
            System.out.println("Subscription created successfully with ID: " + createdSubscription.id);
            System.out.println("Subscription expiry: " + createdSubscription.expirationDateTime);
            return createdSubscription;
        } 
        catch (Exception e) {
            System.err.println("Error creating subscription: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
