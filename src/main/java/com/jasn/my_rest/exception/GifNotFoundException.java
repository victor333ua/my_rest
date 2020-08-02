package com.jasn.my_rest.exception;

public class GifNotFoundException extends RuntimeException {
    public GifNotFoundException(String message) {
        super(message);
    }
}
