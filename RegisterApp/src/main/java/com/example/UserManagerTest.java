package com.example;

public class UserManagerTest 
{
    public static void main(String[] args) 
    {
        UserManager userManager = new UserManager();
        boolean success = userManager.createUser("Dar", "Password123");
        System.out.println("User creation success: " + success);
    }
}
