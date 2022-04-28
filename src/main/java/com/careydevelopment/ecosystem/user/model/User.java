package com.careydevelopment.ecosystem.user.model;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import us.careydevelopment.ecosystem.jwt.model.BaseUser;

@Document(collection = "#{@environment.getProperty('mongo.user.collection')}")
public class User extends BaseUser implements UserDetails {

    private static final long serialVersionUID = 3592549577903104696L;

    /**
     * 名字 所允许的特殊字符：@'/&#,.- ，长度为1-50
     */
    @Pattern(regexp = "^[A-Za-z0-9 '/&#,.-]*$", message = "The only special characters allowed are: +@'/&#,.-")
    @NotBlank(message = "Please provide a first name")
    @Size(max = 50, message = "Please enter a first name between 1 and 50 characters")
    private String firstName;

    /**
     * 姓氏 所允许的特殊字符：@'/&#,.- ，长度为1-50
     */
    @Pattern(regexp = "^[A-Za-z0-9 '/&#,.-]*$", message = "The only special characters allowed are: +@'/&#,.-")
    @NotBlank(message = "Please provide a last name")
    @Size(max = 50, message = "Please enter a last name between 1 and 50 characters")
    private String lastName;

    /**
     * 街道地址 所允许的特殊字符：@'/&#,.- ，长度为1-80
     */
    @Pattern(regexp = "^[A-Za-z0-9 '/&#,.-]*$", message = "The only special characters allowed are: +@'/&#,.-")
    // @NotBlank(message = "Please provide a street address")
    @Size(max = 80, message = "Please enter a street address between 1 and 80 characters")
    private String street1;

    /**
     * 街道地址2 所允许的特殊字符：@'/&#,.- ，长度为1-80
     */
    @Pattern(regexp = "^[A-Za-z0-9 '/&#,.-]*$", message = "The only special characters allowed are: +@'/&#,.-")
    @Size(max = 80, message = "Please enter a street address 2 between 1 and 80 characters")
    private String street2;

    /**
     * 城市 所允许的特殊字符：@'/&#,.- ，长度为1-50
     */
    @Pattern(regexp = "^[A-Za-z0-9 '/&#,.-]*$", message = "The only special characters allowed are: +@'/&#,.-")
    // @NotBlank(message = "Please provide a city")
    @Size(max = 50, message = "Please enter a city between 1 and 50 characters")
    private String city;

    /**
     * 州的两个字母缩写 只允许字母，长度2
     */
    @Pattern(regexp = "^[A-Za-z]*$", message = "Only letters allowed for state")
    // @NotBlank(message = "Please provide a state")
    @Size(max = 2, message = "Please enter a two-letter abbreviation for the state")
    private String state;

    /**
     * 邮政编码 只允许数字、字母和破折号，长度15
     */
    @Pattern(regexp = "^[A-Za-z0-9 -]*$", message = "Only numbers, letters, and dashes allowed for postal code")
    // @NotBlank(message = "Please provide a postal code")
    @Size(max = 15, message = "Please enter a postal code that does not exceed 15 characters")
    private String zip;

    /**
     * 邮箱地址  @Email邮箱格式，
     */
    @NotBlank(message = "Please provide an email address")
    @Email(message = "Please provide a valid email address")
    private String email;

    /**
     * 手机号码 不超过15字符，
     */
    @Pattern(regexp = "^[A-Za-z0-9 +()-]*$", message = "Please enter a valid phone number")
    @NotBlank(message = "Please provide a phone number")
    @Size(max = 15, message = "Please enter a phone number that does not exceed 15 characters")
    private String phoneNumber;

    /**
     * 邮箱地址  只允许数字 国家的两位数缩写
     */
    @Pattern(regexp = "^[A-Za-z]*$", message = "Only letters allowed for country")
    // @NotBlank(message = "Please provide a country")
    @Size(max = 2, message = "Please enter a two-digit abbreviation for country")
    private String country;

    /**
     *时区  只允许字母、数字和斜线  不超过40字符
     */
    @Pattern(regexp = "^[A-Za-z0-9 /]*$", message = "Only letters, numbers, and slashes allowed for timezone")
    @Size(max = 40, message = "Time zone cannot be more than 40 characters")
    private String timezone;

    /**
     * @JsonIgnore 的作用是“在实体类向前台返回数据时用来忽略不想传递给前台的属性或接口
     * 密码
     */
    @JsonIgnore
    private String password;

    /**
     * @JsonIgnore 的作用是“在实体类向前台返回数据时用来忽略不想传递给前台的属性或接口
     * 谷歌api
     */
    @JsonIgnore
    private GoogleApi googleApi;

    /**
     * 邮箱整合
     */
    private EmailIntegration emailIntegration;

    /*
     * 对应set，get 方法
     */
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getStreet1() {
        return street1;
    }

    public void setStreet1(String street1) {
        this.street1 = street1;
    }

    public String getStreet2() {
        return street2;
    }

    public void setStreet2(String street2) {
        this.street2 = street2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public GoogleApi getGoogleApi() {
        return googleApi;
    }

    public void setGoogleApi(GoogleApi googleApi) {
        this.googleApi = googleApi;
    }

    public EmailIntegration getEmailIntegration() {
        return emailIntegration;
    }

    public void setEmailIntegration(EmailIntegration emailIntegration) {
        this.emailIntegration = emailIntegration;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    /**
     * 账户是否过期
     *
     */
    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 账户锁定状态
     *
     */
    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * isCredentialsNonExpired方法返回值设为true，代表密码没有过期。
     * isCredentialsNonExpired方法，但是返回了false，代表密码已经过期了。
     *
     */
    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 是否禁用
     *
     */
    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }
    /**
     * 授权列表
     *
     */
    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> list = authorityNames.stream().map(auth -> new SimpleGrantedAuthority(auth))
                .collect(Collectors.toList());

        return list;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        if (id == null) {
            if (other.getId() != null)
                return false;
        } else if (!id.equals(other.getId()))
            return false;

        return true;
    }
}
