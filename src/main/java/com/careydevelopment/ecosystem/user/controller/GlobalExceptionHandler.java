package com.careydevelopment.ecosystem.user.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.careydevelopment.ecosystem.user.exception.InvalidRequestException;
import com.careydevelopment.ecosystem.user.exception.ServiceException;
import com.careydevelopment.ecosystem.user.exception.UnauthorizedUpdateException;

import us.careydevelopment.util.api.model.IRestResponse;
import us.careydevelopment.util.api.model.ValidationError;
import us.careydevelopment.util.api.response.ResponseEntityUtil;

/**
 * 全局异常处理
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 服务异常
     * @param se
     * @return ResponseEntity
     */
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<IRestResponse<Void>> serviceException(ServiceException se) {
        return ResponseEntityUtil.createResponseEntityWithError(se.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    /**
     * 注册无效异常
     * @param ex 无效请求异常
     * @return ResponseEntityUtil
     */
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<IRestResponse<List<ValidationError>>> invalidRegistrant(
            InvalidRequestException ex) {
        List<ValidationError> errors = ex.getErrors();
        return ResponseEntityUtil.createResponseEntityWithValidationErrors(errors);
    }

    /**
     * 未经授权的更新
     * @param ex
     * @return ResponseEntityUtil
     */
    @ExceptionHandler(UnauthorizedUpdateException.class)
    public ResponseEntity<IRestResponse<Void>> unauthorizedUpdate(
            UnauthorizedUpdateException ex) {
        return ResponseEntityUtil.createResponseEntityWithUnauthorized(ex.getMessage());
    }
}
