package com.example;

import java.security.SecureRandom;
import java.security.MessageDigest;

public class PasswordUtil {
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_+=<>?";
    private static final SecureRandom RANDOM = new SecureRandom();
    public static String hashPassword(String password) throws Exception
    {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(password.getBytes("UTF-8"));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
    public static String generateSecurePassword(int length) 
    {
        if (length < 8) 
            throw new IllegalArgumentException("Password length must be at least 8");
        StringBuilder password = new StringBuilder(length);
        password.append(randomChar(UPPER));
        password.append(randomChar(LOWER));
        password.append(randomChar(DIGITS));
        password.append(randomChar(SYMBOLS));
        String allChars = UPPER + LOWER + DIGITS + SYMBOLS;
        for (int i = 4; i < length; i++) {
            password.append(randomChar(allChars));
        }
        char[] pwChars = password.toString().toCharArray();
        for (int i = pwChars.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char temp = pwChars[i];
            pwChars[i] = pwChars[j];
            pwChars[j] = temp;
        }
        return new String(pwChars);
    }
    private static char randomChar(String s) {
        return s.charAt(RANDOM.nextInt(s.length()));
    }
}
