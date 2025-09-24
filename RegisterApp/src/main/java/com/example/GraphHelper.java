package com.example;

import com.microsoft.graph.models.User;

public class GraphHelper {
    private AzureUserManager azureUserManager;
    public GraphHelper(AzureUserManager azureUserManager)
    {
        this.azureUserManager = azureUserManager;
    }
    public User getUserDetailsById(String userId)
    {
        try 
        {
            return azureUserManager.getGraphClient().users(userId)
                    .buildRequest()
                    .get();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            return null;
        }
    }
}
