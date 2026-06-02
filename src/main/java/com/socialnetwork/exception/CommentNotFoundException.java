package com.socialnetwork.exception;

public class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException() { super(); }
    public CommentNotFoundException(String message) { super(message); }
    public CommentNotFoundException(String message, Throwable cause) { super(message, cause); }
}
