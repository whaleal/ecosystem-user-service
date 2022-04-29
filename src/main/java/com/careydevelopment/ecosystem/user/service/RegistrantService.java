package com.careydevelopment.ecosystem.user.service;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.careydevelopment.ecosystem.user.exception.EmailCodeCreateFailedException;
import com.careydevelopment.ecosystem.user.exception.InvalidRequestException;
import com.careydevelopment.ecosystem.user.exception.ServiceException;
import com.careydevelopment.ecosystem.user.exception.TextCodeCreateFailedException;
import com.careydevelopment.ecosystem.user.exception.UserSaveFailedException;
import com.careydevelopment.ecosystem.user.model.Registrant;
import com.careydevelopment.ecosystem.user.model.RegistrantAuthentication;
import com.careydevelopment.ecosystem.user.model.User;
import com.careydevelopment.ecosystem.user.model.UserSearchCriteria;
import com.careydevelopment.ecosystem.user.repository.RegistrantAuthenticationRepository;
import com.careydevelopment.ecosystem.user.repository.UserRepository;
import com.careydevelopment.ecosystem.user.util.TotpUtil;
import com.careydevelopment.ecosystem.user.util.UserUtil;

import us.careydevelopment.ecosystem.jwt.util.RecaptchaUtil;
import us.careydevelopment.util.api.model.ValidationError;
import us.careydevelopment.util.api.validation.ValidationUtil;
import us.careydevelopment.util.date.DateConversionUtil;

/**
 * 注册服务层
 */
@Service
public class RegistrantService {

    private static final Logger LOG = LoggerFactory.getLogger(RegistrantService.class);

    private static final int MAX_MINUTES_FOR_CODE = 5;
    private static final int MAX_FAILED_ATTEMPTS = 5;

    @Value("${recaptcha.active}")
    String recaptchaActive;

    //自动注入

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecaptchaUtil recaptchaUtil;

    @Autowired
    private TotpUtil totpUtil;

    @Autowired
    private RegistrantAuthenticationRepository registrantAuthenticationRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private UserUtil userUtil;

    /**
     * 增加权限
     * @param username 用户名
     * @param authority 权限
     */
    public void addAuthority(String username, String authority) {
        try {
            //获取用户
            User user = userRepository.findByUsername(username);

            //如果用户不为空 进行权限增加
            if (user != null) {
                user.getAuthorityNames().add(authority);
                userRepository.save(user);
            }
        } catch (Exception e) {
            LOG.error("Problem adding authority!", e);
            //添加权限时异常
            throw new ServiceException("Problem adding authority!");
        }
    }

    /**
     * 验证文本代码
     * @param auth 注册者
     * @param code 验证码
     * @return boolean
     */
    public boolean validateTextCode(RegistrantAuthentication auth, String code) {
        boolean verified = false;
        LOG.debug("Failed attempts for " + auth.getUsername() + " is " + auth.getFailedAttempts());

        try {
            //判断尝试次数，当小于5时
            if (auth.getFailedAttempts() < MAX_FAILED_ATTEMPTS) {
                String requestId = auth.getRequestId();
                //验证验证码是否正确
                verified = smsService.checkValidationCode(requestId, code);

                if (!verified) {
                    //不正确失败次数加一
                    auth.setFailedAttempts(auth.getFailedAttempts() + 1);
                    registrantAuthenticationRepository.save(auth);
                }
            }
        } catch (Exception e) {
            //日志，异常处理
            LOG.error("Problem validating text code!", e);
            throw new ServiceException("Problem validating text code!");
        }

        return verified;
    }

    /**
     * 验证邮件代码
     * @param username 用户名
     * @param code 验证码
     * @return boolean
     */
    public boolean validateEmailCode(String username, String code) {
        try {
            //获取验证次数
            int previousAttempts = getPreviousAttempts(username, RegistrantAuthentication.Type.EMAIL);

            //次数小于失败最大数时进行操作，大于最大数日志输出：超过了验证电子邮件代码的最大尝试次数 返回false
            if (previousAttempts < MAX_FAILED_ATTEMPTS) {
                List<RegistrantAuthentication> list = validateCode(username, code, RegistrantAuthentication.Type.EMAIL);
    
                if (list != null && list.size() > 0) {
                    return true;
                } else {
                    //验证码为空递增失败次数
                    incrementFailedAttempts(username, RegistrantAuthentication.Type.EMAIL);
                    return false;
                }
            } else {
                LOG.debug("User " + username + " exceeded max attempts to validate the email code");
                return false;
            }
        } catch (Exception e) {
            //日至输出，异常处理
            LOG.error("Problem validating email code!", e);
            throw new ServiceException("Problem validating email code!");
        }
    }

    /**
     * 获取尝试次数
     * @param username 用户名
     * @param type 验证类型
     * @return int
     */
    private int getPreviousAttempts(String username, RegistrantAuthentication.Type type) {
        int previousAttempts = 0;
        LOG.debug("Checking previous attempts for " + username);

        //获取注册者列表
        List<RegistrantAuthentication> auths = registrantAuthenticationRepository
                .findByUsernameAndTypeOrderByTimeDesc(username, type.toString());

        //判断是否为空，为空直接返回0，不为空获取失败次数进行赋值
        if (auths != null && auths.size() > 0) {
            RegistrantAuthentication auth = auths.get(0);
            previousAttempts = auth.getFailedAttempts();
        }

        LOG.debug("Failed attempts is " + previousAttempts);
        return previousAttempts;
    }

    /**
     * 递增失败尝试次数
     * @param username 用户名
     * @param type 注册类型
     */
    private void incrementFailedAttempts(String username, RegistrantAuthentication.Type type) {
        //获取注册认证
        List<RegistrantAuthentication> auths = registrantAuthenticationRepository
                .findByUsernameAndTypeOrderByTimeDesc(username, type.toString());

        //判断认证是否为空
        if (auths != null && auths.size() > 0) {
            RegistrantAuthentication auth = auths.get(0);
            auth.setFailedAttempts(auth.getFailedAttempts() + 1);

            registrantAuthenticationRepository.save(auth);
        }
    }

    /**
     * 验证代码
     * @param username 用户名
     * @param code 验证码
     * @param type 验证类型
     * @return List
     */
    private List<RegistrantAuthentication> validateCode(String username, String code,
            RegistrantAuthentication.Type type) {

        //获取时间  毫秒级别
        long time = System.currentTimeMillis()
                - (DateConversionUtil.NUMBER_OF_MILLISECONDS_IN_MINUTE * MAX_MINUTES_FOR_CODE);

        //获取检查验证码List
        List<RegistrantAuthentication> auths = registrantAuthenticationRepository.codeCheck(username, time,
                type.toString(), code);

        return auths;
    }

    /**
     创建文本验证码
     * @param username 用户名
     */
    public void createTextCode(String username) {
        //根据用户名获取用户
        User user = userRepository.findByUsername(username);
        LOG.debug("Found user is " + user);

        //用户不为空进行信息的发送
        if (user != null) {
            try {
                //发送信息
                String requestId = smsService.sendValidationCode(user.getPhoneNumber());

                if (requestId != null) {
                    RegistrantAuthentication auth = new RegistrantAuthentication();
                    //设置注册认证信息
                    auth.setUsername(username);
                    auth.setTime(System.currentTimeMillis());
                    auth.setType(RegistrantAuthentication.Type.TEXT);
                    auth.setRequestId(requestId);

                    //对注册认证信息保存
                    registrantAuthenticationRepository.save(auth);
                } else {
                    //信息为空日志输出 抛出异常
                    LOG.error("Unable to create text code as user " + username + " doesn't exist");
                    throw new TextCodeCreateFailedException("User " + username + " doesn't exist");
                }
            } catch (Exception e) {
                //异常捕获
                LOG.error("Problem creating text code!", e);
                throw new TextCodeCreateFailedException(e.getMessage());
            }
        }
    }

    /**
     * 创建邮箱验证码
     * @param registrant 注册者
     */
    public void createEmailCode(Registrant registrant) {
        try {
            //创建验证码
            String code = createCode(registrant.getUsername(), RegistrantAuthentication.Type.EMAIL);

            //验证体
            String validationBody = "\n\nYour verification code for Carey Development, LLC and the CarEcosystem Network.\n\n"
                    + "Use verification code: " + code;

            //发送验证码
            emailService.sendSimpleMessage(registrant.getEmailAddress(), "Your Verification Code", validationBody);
        } catch (Exception e) {
            //异常捕获
            LOG.error("Problem creating email code!", e);
            throw new EmailCodeCreateFailedException(e.getMessage());
        }
    }

    /**
     * 创建验证码
     * @param username 用户名
     * @param type 注册类型
     * @return 验证码
     */
    private String createCode(String username, RegistrantAuthentication.Type type) {
        RegistrantAuthentication auth = new RegistrantAuthentication();
        //设置注册认证信息
        auth.setUsername(username);
        auth.setTime(System.currentTimeMillis());
        auth.setType(type);

        //获取验证码 设置验证码
        String code = totpUtil.getTOTPCode();
        auth.setCode(code);

        //保存
        registrantAuthenticationRepository.save(auth);

        return code;
    }

    /**
     * 保存用户
     * @param registrant 注册者
     * @return User
     */
    public User saveUser(Registrant registrant) {
        User savedUser = null;

        try {
            //将注册人转换为用户
            User user = userUtil.convertRegistrantToUser(registrant);
            //保存
            savedUser = userRepository.save(user);
        } catch (Exception e) {
            //异常捕获
            LOG.error("Problem saving user!", e);
            throw new UserSaveFailedException(e.getMessage());
        }

        return savedUser;
    }

    /**
     * 验证注册
     * @param registrant 注册者
     * @param errors 验证错误
     */
    public void validateRegistrant(Registrant registrant, List<ValidationError> errors) {
        handleRemainingValidations(registrant, errors);
        LOG.debug("validation is " + errors);

        //当有验证错误是，抛出异常提示无效的注册
        if (errors.size() > 0) {
            throw new InvalidRequestException("Invalid registrant", errors);
        }
    }

    /**
     * 处理程序验证
     * @param registrant 注册者
     * @param errors 验证错误
     */
    private void handleRemainingValidations(Registrant registrant, List<ValidationError> errors) {
        try {
            //验证唯一名称
            validateUniqueName(errors, registrant);
            //验证唯一email
            validateUniqueEmail(errors, registrant);
            //验证校验码
            validateRecaptcha(errors, registrant);
        } catch (Exception e) {
            //抛出异常，日志输出
            LOG.error("Problem validating registrant!", e);
            throw new ServiceException("Problem validating registrant!");
        }
    }

    /**
     * 重新验证校验码
     * @param errors 严恒错误
     * @param registrant 注册者
     * @throws IOException
     */
    private void validateRecaptcha(List<ValidationError> errors, Registrant registrant) throws IOException {
        //todo
        if (!StringUtils.isBlank(recaptchaActive) && !recaptchaActive.equalsIgnoreCase("false")) {
            float score = recaptchaUtil.createAssessment(registrant.getRecaptchaResponse());

            if (score < RecaptchaUtil.RECAPTCHA_MIN_SCORE) {
                // user-friendly error message not necessary if a bot is trying to get in 如果没有必要尝试在bot中获取用户友好的消息
                ValidationUtil.addError(errors, "Google thinks you're a bot", null, null);
            }
        }
    }

    /**
     * 验证唯一名称
     * @param errors 验证错误
     * @param registrant 注册者
     */
    private void validateUniqueName(List<ValidationError> errors, Registrant registrant) {
        //获取注册者用户名
        String username = registrant.getUsername();

        if (!StringUtils.isBlank(username)) {
            //获取搜索标准
            UserSearchCriteria searchCriteria = new UserSearchCriteria();
            searchCriteria.setUsername(username.trim());

            //按条件搜索用户
            List<User> users = userService.search(searchCriteria);
            if (users.size() > 0) {
                //添加错误信息 用户名已被占用
                ValidationUtil.addError(errors, "Username is taken", "username", "usernameTaken");
            }
        }
    }

    /**
     * 验证唯一email
     * @param errors 验证错误
     * @param registrant 注册者
     */
    private void validateUniqueEmail(List<ValidationError> errors, Registrant registrant) {
        //获取注册用户邮箱
        String email = registrant.getEmailAddress();

        if (!StringUtils.isBlank(email)) {
            //获取搜索条件
            UserSearchCriteria searchCriteria = new UserSearchCriteria();
            searchCriteria.setEmailAddress(email.trim());
            //按条件搜索email
            List<User> users = userService.search(searchCriteria);
            if (users.size() > 0) {
                //添加错误信息，email已被占用
                ValidationUtil.addError(errors, "Email address is taken", "emailAddress", "emailTaken");
            }
        }
    }
}
