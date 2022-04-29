package com.careydevelopment.ecosystem.user.util;

import com.careydevelopment.ecosystem.user.model.User;

import us.careydevelopment.ecosystem.file.FileNameUtil;

/**
 * 用户文件名工具类
 */
public class UserFileNameUtil extends FileNameUtil {

    //新建文件名
    public static String createFileName(User user, String originalFileName) {
        String fileName = createTimestampedUniqueFileName(user.getId());

        fileName = appendExtensionFromOriginalFileName(fileName, originalFileName);

        return fileName;
    }
}
