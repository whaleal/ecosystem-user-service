package com.careydevelopment.ecosystem.user.exception;

/**
 * 服务异常，运行时异常
 */
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 8554361118283396759L;

    public ServiceException(String s) {
        super(s);
    }
}
