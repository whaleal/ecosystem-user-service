package com.careydevelopment.ecosystem.user.exception;


/**
 *文本代码创建异常，运行时异常
 */
public class TextCodeCreateFailedException extends RuntimeException {

    private static final long serialVersionUID = -460034748688687252L;

    public TextCodeCreateFailedException(String s) {
        super(s);
    }
}
