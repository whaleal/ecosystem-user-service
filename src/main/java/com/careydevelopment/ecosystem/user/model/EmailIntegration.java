package com.careydevelopment.ecosystem.user.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * 邮箱整合，邮箱实体
 */
public class EmailIntegration {

    //邮箱类型
    private EmailIntegrationType integrationType;


    /*
        set,get 方法
     */

    public EmailIntegrationType getIntegrationType() {
        return integrationType;
    }

    public void setIntegrationType(EmailIntegrationType integrationType) {
        this.integrationType = integrationType;
    }

    //toString
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

}
