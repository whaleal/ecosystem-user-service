package com.careydevelopment.ecosystem.user.exception;

import java.util.List;

import us.careydevelopment.util.api.model.ValidationError;

/**
 * 无效请求异常，运行时异常
 */
public class InvalidRequestException extends RuntimeException {

    private static final long serialVersionUID = 9155139576610874161L;

    private List<ValidationError> errors;
    
    public InvalidRequestException(String message, List<ValidationError> errors) {
        super(message);
        this.errors = errors;
    }
    
    public List<ValidationError> getErrors() {
        return errors;
    }
}
