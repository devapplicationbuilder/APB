package org.quickdev.sdk.exception;

/**
 * marker interface
 */
public class BaseException extends RuntimeException {
    public BaseException(String message) {
        super(message);
    }
}
