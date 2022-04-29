package com.careydevelopment.ecosystem.user.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 注册者
 * @NotNull 注释不能为空，为空则提示message内容
 */
public class Registrant {

    /**
     * 名  1-32字符
     */
    @NotNull
    @Size(min = 1, max = 32, message = "First name must be between 1 and 32 characters")
    private String firstName;

    /**
     * 姓  1-32字符
     */
    @NotNull
    @Size(min = 1, max = 32, message = "Last name must be between 1 and 32 characters")
    private String lastName;

    /**
     * 用户名  5-12字符
     */
    @NotNull
    @Size(min = 5, max = 12, message = "Username must be between 5 and 12 characters")
    private String username;

    /**
     * 密码  8-20字符
     */
    @NotNull
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    private String password;

    /**
     * email地址
     */
    @NotNull
    @Pattern(regexp = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$", message = "Email address is invalid")
    private String emailAddress;

    /**
     * 手机号码
     */
    @NotNull
    private String phone;

    @JsonProperty("g-recaptcha-response")
    private String recaptchaResponse;

    /*
        对应set，get方法
     */

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRecaptchaResponse() {
        return recaptchaResponse;
    }

    public void setRecaptchaResponse(String recaptchaResponse) {
        this.recaptchaResponse = recaptchaResponse;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
