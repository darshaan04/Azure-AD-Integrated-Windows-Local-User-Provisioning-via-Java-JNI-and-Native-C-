package com.example;


public class UserManager {
    static {
        System.loadLibrary("localuser");
    }
    public native boolean createUser(String username, String password);
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java LocalUserCreator <username> <password>");
            return;
        }

        UserManager creator = new UserManager();
        boolean success = creator.createUser(args[0], args[1]);

        System.out.println("User creation " + (success ? "succeeded" : "failed"));
    }

}