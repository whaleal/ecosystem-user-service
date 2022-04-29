package com.careydevelopment.ecosystem.user.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * 用户搜索标准
 */
public class UserSearchCriteria {

    //email地址
    private String emailAddress;
    //用户名
    private String username;

    /*
        对应set，get 方法
     */

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    //toString
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

}
