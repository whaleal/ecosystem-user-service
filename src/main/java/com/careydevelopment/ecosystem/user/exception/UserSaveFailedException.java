package com.careydevelopment.ecosystem.user.exception;

/**
 * 用户保存失败异常，运行时异常
 */
public class UserSaveFailedException extends RuntimeException {

    private static final long serialVersionUID = 132624850105946905L;

    public UserSaveFailedException(String s) {
        super(s);
    }
}
