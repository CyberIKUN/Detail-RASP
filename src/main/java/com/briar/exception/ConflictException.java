package com.briar.exception;


/**
 * 白名单和黑名单冲突异常
 */
public class ConflictException extends Exception{
    public ConflictException(String message) {
        super(message);
    }
}
