package com.shop.exception;

// thin wrapper kept for readability; controllers/services mostly throw ResponseStatusException directly
public class ApiException extends RuntimeException {
    public ApiException(String message) { super(message); }
}
