package com.careydevelopment.ecosystem.user.exception;

/**
 * 用户未找到异常，运行时异常
 */
public class UserNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -2234419646899887299L;

    public UserNotFoundException(String s) {
        super(s);
    }
}
