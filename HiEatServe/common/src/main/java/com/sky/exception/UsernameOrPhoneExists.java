package com.sky.exception;

public class UsernameOrPhoneExists extends RuntimeException {
    public UsernameOrPhoneExists(String message) {
        super(message);
    }
}
