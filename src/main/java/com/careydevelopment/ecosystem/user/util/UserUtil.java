package com.careydevelopment.ecosystem.user.util;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import com.careydevelopment.ecosystem.user.exception.InvalidRequestException;
import com.careydevelopment.ecosystem.user.exception.UnauthorizedUpdateException;
import com.careydevelopment.ecosystem.user.exception.UserNotFoundException;
import com.careydevelopment.ecosystem.user.model.Registrant;
import com.careydevelopment.ecosystem.user.model.User;
import com.careydevelopment.ecosystem.user.repository.UserRepository;

import us.careydevelopment.util.api.model.ValidationError;
import us.careydevelopment.util.api.validation.ValidationUtil;

/**
 * user工具类
 */
@Component
public class UserUtil {
    
    private static final Logger LOG = LoggerFactory.getLogger(UserUtil.class);

    //自动注入

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private SecurityUtil securityUtil;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * 转换注册者用户
     * @param registrant 注册者
     * @return User
     */
    public User convertRegistrantToUser(Registrant registrant) {
        //创建user
        User user = new User();
        //设置email
        user.setEmail(registrant.getEmailAddress());
        //设置名
        user.setFirstName(registrant.getFirstName());
        //设置姓
        user.setLastName(registrant.getLastName());
        //设置密码
        user.setPassword(encoder.encode(registrant.getPassword()));
        //设置用户名
        user.setUsername(registrant.getUsername());
        //设置电话号码
        user.setPhoneNumber(registrant.getPhone());

        return user;
    }

    /**
     *验证用户更新，
     * @param user
     * @param bindingResult
     */
    public void validateUserUpdate(User user, BindingResult bindingResult) {

        //获取用户安全状态
        boolean allowed = securityUtil.isAuthorizedByUserId(user.getId());

        //判断状态，如果为true则进行绑定，false则抛出异常
        if (allowed) {
            if (bindingResult.hasErrors()) {
                LOG.error("Binding result: " + bindingResult);
                
                List<ValidationError> errors = ValidationUtil.convertBindingResultToValidationErrors(bindingResult);
                throw new InvalidRequestException("Invalid user request", errors);
            }
        } else {
            LOG.error("Not allowed to update user ID " + user.getId());
            throw new UnauthorizedUpdateException("Not allowed to update user ID " + user.getId());
        }
    }

    /**
     * 根据id获取删除用户
     * @param userId
     * @return
     */
    public User validateUserDelete(String userId) {
        //获取用户状态
        boolean allowed = securityUtil.isAuthorizedByUserId(userId);

        //判断状态，如果为true根据userID查找user，判断userOpt不为空返回user，为空抛出异常
        if (allowed) {
            Optional<User> userOpt = userRepository.findById(userId);
            
            if (userOpt.isEmpty()) {
                throw new UserNotFoundException("User " + userId + " doesn't exist");
            } else {
                return userOpt.get();
            }
        } else {
            LOG.debug("Not allowed to delete user ID " + userId);
            throw new UnauthorizedUpdateException("Not allowed to delete another user");
        }
    }
}
