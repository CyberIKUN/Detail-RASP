package com.briar.exception;

/**
 * 阻塞攻击异常
 */
public class BlockAttackException extends Exception{
    public BlockAttackException(String message) {
        super(message);
    }
}
