package com.careydevelopment.ecosystem.user.exception;

/**
 * 未经授权的更新异常,运行时异常
 */
public class UnauthorizedUpdateException extends RuntimeException {

    private static final long serialVersionUID = 2766148850385132820L;

    public UnauthorizedUpdateException(String s) {
        super(s);
    }
}
