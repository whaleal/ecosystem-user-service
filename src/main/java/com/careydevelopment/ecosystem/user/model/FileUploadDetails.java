package com.careydevelopment.ecosystem.user.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * 文件上传细节
 */
public class FileUploadDetails {

    //上传文件大小
    private long uploadFileSize;


    /*
        对应set， get 方法
     */

    public long getUploadFileSize() {
        return uploadFileSize;
    }

    public void setUploadFileSize(long uploadFileSize) {
        this.uploadFileSize = uploadFileSize;
    }

    //toString
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
