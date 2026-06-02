package com.socialnetwork.exception;

public class CategoryNotFoundException extends Exception {
    public CategoryNotFoundException() { super(); }
    public CategoryNotFoundException(String message) { super(message); }
    public CategoryNotFoundException(String message, Throwable cause) { super(message, cause); }
}
