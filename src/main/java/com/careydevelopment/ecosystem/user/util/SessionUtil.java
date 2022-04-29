package com.careydevelopment.ecosystem.user.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.careydevelopment.ecosystem.user.model.User;

/**
 * session工具类
 */
//元注解（可以注解其他注解的注解）
@Component
public class SessionUtil {

    /**
     *获取当前用户
     * @return
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        return user;
    }

}
