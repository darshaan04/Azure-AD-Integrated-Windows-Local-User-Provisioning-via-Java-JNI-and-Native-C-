package com.example;

public class WebhookInitializer {
	public static void initializeWebhookSubscription()
	{
	    String ngrokUrl = NgrokUrlFetcher.getNgrokHttpsUrl();
	    if (ngrokUrl != null)
	    {
	        try
	        {
	            String appContextPath = "/RegisterApp-1.0-SNAPSHOT";
	            String webhookPath = "/graph-webhook";
	            String fullWebhookUrl = ngrokUrl + appContextPath + webhookPath;
	            AzureCredentials creds = AzureCredentials.getFromDB();
	            SubscriptionManager subMgr = new SubscriptionManager(creds.tenantId, creds.clientId, creds.clientSecret);
	            subMgr.createOrUpdateSubscription(fullWebhookUrl);
	            System.out.println("Webhook subscription created/updated with URL: " + fullWebhookUrl);
	        }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	        }
	    } 
	    else 
	    {
	        System.err.println("Failed to obtain ngrok HTTPS URL");
	    }
	}

    public static void main(String[] args) 
    {
        initializeWebhookSubscription();
    }
}
