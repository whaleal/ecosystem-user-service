package com.careydevelopment.ecosystem.user.exception;


/**
 * 邮箱代码创建异常，继承运行时异常
 */
public class EmailCodeCreateFailedException extends RuntimeException {

    private static final long serialVersionUID = -460034748688687252L;

    public EmailCodeCreateFailedException(String s) {
        super(s);
    }
}
