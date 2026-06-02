package com.socialnetwork.exception;

public class InvalidInputException extends RuntimeException {
    public InvalidInputException() { super(); }
    public InvalidInputException(String message) { super(message); }
    public InvalidInputException(String message, Throwable cause) { super(message, cause); }
}
