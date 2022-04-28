package com.careydevelopment.ecosystem.user.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.careydevelopment.ecosystem.user.model.User;

import us.careydevelopment.ecosystem.jwt.constants.Authority;

@Component
public class SecurityUtil {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityUtil.class);

    @Autowired
    private SessionUtil sessionUtil;

    /**
     * 通过用户id验证
     * @param userId
     * @return
     */
    public boolean isAuthorizedByUserId(String userId) {
        //初始值为false，当user为null时直接返回false
        boolean authorized = false;

        //获取当前用户
        User user = sessionUtil.getCurrentUser();

        if (user != null && userId != null) {
            if (user.getId() != null) {
                //将当前登录用户id与参数id对比
                if (user.getId().equals(userId)) {
                    authorized = true;
                } else {
                    //if the user is an admin, can do anything
                    //判断是否为admin用户
                    if (user.getAuthorityNames() != null && user.getAuthorityNames().contains(Authority.ADMIN_ECOSYSTEM_USER)) {
                        authorized = true;
                    }
                }
            }
        }

        return authorized;
    }
}
