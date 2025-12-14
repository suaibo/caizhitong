package com.mint.caizhitong.common.exception;

/**
 * @author Mint
 */
public class BusinessConflictException extends RuntimeException {
    public BusinessConflictException(String message) {
        super(message);
    }

    public BusinessConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
