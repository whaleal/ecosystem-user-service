package com.careydevelopment.ecosystem.user.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.careydevelopment.ecosystem.user.model.User;

import us.careydevelopment.ecosystem.file.FileUtil;
import us.careydevelopment.ecosystem.file.exception.CopyFileException;
import us.careydevelopment.ecosystem.file.exception.FileTooLargeException;
import us.careydevelopment.ecosystem.file.exception.ImageRetrievalException;
import us.careydevelopment.ecosystem.file.exception.MissingFileException;

/**
 * 用户文件工具类
 */
@Component
public class UserFileUtil extends FileUtil {

    private static final Logger LOG = LoggerFactory.getLogger(FileUtil.class);

    public UserFileUtil(@Value("${user.files.base.path}") String userFilesBasePath,
            @Value("${max.file.upload.size}") Long maxFileUploadSize) {
        this.maxFileUploadSize = maxFileUploadSize;
        this.userFilesBasePath = userFilesBasePath;
    }

    /**
     * 按用户ID获取个人资料照片
     * @param userId
     * @return Path
     * @throws ImageRetrievalException
     */
    public Path fetchProfilePhotoByUserId(String userId) throws ImageRetrievalException {
        Path imagePath = null;

        //获取路径
        Path rootLocation = Paths.get(getRootLocationForUserProfileImageUpload(userId));
        LOG.debug("Fetching profile image from " + rootLocation.toString());

        try {
            if (rootLocation.toFile().exists()) {
                Iterator<Path> iterator = Files.newDirectoryStream(rootLocation).iterator();

                if (iterator.hasNext()) {
                    //获取文件名
                    imagePath = iterator.next();
                    LOG.debug("File name is " + imagePath);
                }
            }
        } catch (IOException ie) {
            throw new ImageRetrievalException(ie.getMessage());
        }

        return imagePath;
    }

    /**
     * 保存个人资料照片
     * @param file 照片
     * @param user
     * @throws MissingFileException
     * @throws FileTooLargeException
     * @throws CopyFileException
     */
    public void saveProfilePhoto(MultipartFile file, User user)
            throws MissingFileException, FileTooLargeException, CopyFileException {
        validateFile(file, maxFileUploadSize);
        Path rootLocation = Paths.get(getRootLocationForUserProfileImageUpload(user));
        deleteAllFilesInDirectory(rootLocation);
        saveFile(file, user, rootLocation);
    }

    /**
     * 保存文件
     * @param file 文件
     * @param user 用户
     * @param rootLocation 根位置
     * @throws CopyFileException
     */
    private void saveFile(MultipartFile file, User user, Path rootLocation) throws CopyFileException {
        try (InputStream is = file.getInputStream()) {
            String newFileName = getNewFileName(file, user);
            //使用流将文件复制
            Files.copy(is, rootLocation.resolve(newFileName));
        } catch (IOException ie) {
            LOG.error("Problem uploading file!", ie);
            throw new CopyFileException("Failed to upload!");
        }
    }

    /**
     * 获取新文件名
     * @param file 新文件
     * @param user 用户
     * @return 文件名
     */
    private String getNewFileName(MultipartFile file, User user) {
        LOG.debug("File name is " + file.getOriginalFilename());

        String newFileName = UserFileNameUtil.createFileName(user, file.getOriginalFilename());
        LOG.debug("New file name is " + newFileName);

        return newFileName;
    }

    /**
     * 根据user获取用户上传的根位置
     * @param user 用户
     * @return 路径
     */
    public String getRootLocationForUserUpload(User user) {
        if (user == null)
            throw new IllegalArgumentException("No user provided!");
        return this.getRootLocationForUserUpload(user.getId());
    }

    /**
     * 根据userId获取用户配置文件图像上载的根位置
     * @param userId 用户id
     * @return 路径
     */
    public String getRootLocationForUserProfileImageUpload(String userId) {
        //用户id为空直接抛出异常未找到用户id
        if (StringUtils.isEmpty(userId))
            throw new IllegalArgumentException("No user id!");

        String base = getRootLocationForUserUpload(userId);

        //拼接路径
        StringBuilder builder = new StringBuilder(base);
        builder.append("/");
        builder.append(PROFILE_DIR);

        String location = builder.toString();

        createDirectoryIfItDoesntExist(location);

        return location;
    }

    /**
     * 根据user获取用户配置文件图像上载的根位置
     * @param user 用户
     * @return 路径
     */
    public String getRootLocationForUserProfileImageUpload(User user) {
        if (user == null)
            throw new IllegalArgumentException("No user provided!");
        return this.getRootLocationForUserProfileImageUpload(user.getId());
    }
}
