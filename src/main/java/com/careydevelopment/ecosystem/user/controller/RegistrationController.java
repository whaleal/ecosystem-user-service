package com.careydevelopment.ecosystem.user.controller;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.careydevelopment.ecosystem.user.exception.EmailCodeCreateFailedException;
import com.careydevelopment.ecosystem.user.exception.TextCodeCreateFailedException;
import com.careydevelopment.ecosystem.user.exception.UserSaveFailedException;
import com.careydevelopment.ecosystem.user.model.Registrant;
import com.careydevelopment.ecosystem.user.model.RegistrantAuthentication;
import com.careydevelopment.ecosystem.user.model.User;
import com.careydevelopment.ecosystem.user.repository.RegistrantAuthenticationRepository;
import com.careydevelopment.ecosystem.user.service.RegistrantService;

import us.careydevelopment.ecosystem.jwt.constants.Authority;
import us.careydevelopment.util.api.model.IRestResponse;
import us.careydevelopment.util.api.model.ValidationError;
import us.careydevelopment.util.api.response.ResponseEntityUtil;
import us.careydevelopment.util.api.validation.ValidationUtil;

/**
 * 注册controller
 */
@RestController
public class RegistrationController {

    private static final Logger LOG = LoggerFactory.getLogger(RegistrationController.class);

    //自动注入

    @Autowired
    private RegistrantService registrantService;

    @Autowired
    private RegistrantAuthenticationRepository registrantAuthenticationRepository;

    /**
     * 用户保存失败异常
     * @return
     */
    @ExceptionHandler(UserSaveFailedException.class)
    public ResponseEntity<IRestResponse<Void>> userSaveFailed() {
        return ResponseEntityUtil.createResponseEntityWithError("User save failed!",
                HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    /**
     * email创建失败异常
     * @return
     */
    @ExceptionHandler(EmailCodeCreateFailedException.class)
    public ResponseEntity<IRestResponse<Void>> emailCodeCreateFailed() {
        return ResponseEntityUtil.createResponseEntityWithError("Email code create failed!",
                HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    /**
     * 文本创建异常
     * @return
     */
    @ExceptionHandler(TextCodeCreateFailedException.class)
    public ResponseEntity<IRestResponse<Void>> textCodeCreateFailed() {
        return ResponseEntityUtil.createResponseEntityWithError("Text code create failed!",
                HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    /**
     *电子邮件验证状态
     * @param username
     * @param code
     * @return ResponseEntity
     */
    @GetMapping("/emailVerificationStatus")
    public ResponseEntity<?> getEmailVerificationStatus(@RequestParam String username, @RequestParam String code) {
        LOG.debug("Checking email verification for user " + username + " with code " + code);

        //获取验证状态
        boolean verified = registrantService.validateEmailCode(username, code);

        //判断验证状态，如果为true创建username，返回HttpStatus.NO_CONTENT，不为true返回HttpStatus.NOT_FOUND
        if (verified) {
            registrantService.createTextCode(username);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    /**
     *获取sms验证状态
     * @param username
     * @param code
     * @return ResponseEntity
     */
    @GetMapping("/smsVerificationStatus")
    public ResponseEntity<?> getSmsVerificationStatus(@RequestParam String username, @RequestParam String code) {
        LOG.debug("Checking SMS verification for user " + username + " with code " + code);

        //按照时间顺序根据用户名与类型查找注册验证存入list中
        List<RegistrantAuthentication> auths = registrantAuthenticationRepository
                .findByUsernameAndTypeOrderByTimeDesc(username, RegistrantAuthentication.Type.TEXT.toString());

        //如果list集合为空直接返回未找到，如果不为空则将最新的记录更新
        if (auths != null && auths.size() > 0) {
            // most recent persisted record will be the latest SMS record 最近保存的记录将是最新的SMS记录
            RegistrantAuthentication auth = auths.get(0);

            //获取验证状态
            boolean verified = registrantService.validateTextCode(auth, code);

            //判断段验证状态
            if (verified) {
                //添加授权
                registrantService.addAuthority(username, Authority.BASIC_ECOSYSTEM_USER);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    /**
     * 创建用户
     * @param registrant
     * @param bindingResult
     * @return ResponseEntity
     */
    @PostMapping("/")
    public ResponseEntity<IRestResponse<User>> createUser(@Valid @RequestBody Registrant registrant,
            BindingResult bindingResult) {
        LOG.debug("Registrant is " + registrant);

        //验证列表
        List<ValidationError> validationErrors = ValidationUtil.convertBindingResultToValidationErrors(bindingResult);

        // look for any validations not caught by JSR 380  寻找JSR 380未捕获的任何验证
        registrantService.validateRegistrant(registrant, validationErrors);

        //保存操作
        User savedUser = registrantService.saveUser(registrant);

        //给创建的用户添加emailode
        registrantService.createEmailCode(registrant);

        return ResponseEntityUtil.createSuccessfulResponseEntity("Successfully registered!", HttpStatus.CREATED.value(),
                savedUser);
    }
}
