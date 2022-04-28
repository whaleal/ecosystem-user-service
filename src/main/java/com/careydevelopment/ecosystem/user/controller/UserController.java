package com.careydevelopment.ecosystem.user.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.careydevelopment.ecosystem.user.exception.UserNotFoundException;
import com.careydevelopment.ecosystem.user.model.User;
import com.careydevelopment.ecosystem.user.model.UserSearchCriteria;
import com.careydevelopment.ecosystem.user.repository.UserRepository;
import com.careydevelopment.ecosystem.user.service.UserService;
import com.careydevelopment.ecosystem.user.util.SessionUtil;
import com.careydevelopment.ecosystem.user.util.UserFileUtil;
import com.careydevelopment.ecosystem.user.util.UserUtil;

import us.careydevelopment.ecosystem.file.exception.FileTooLargeException;
import us.careydevelopment.ecosystem.file.exception.MissingFileException;
import us.careydevelopment.ecosystem.jwt.constants.CookieConstants;
import us.careydevelopment.util.api.cookie.CookieUtil;
import us.careydevelopment.util.api.input.InputSanitizer;
import us.careydevelopment.util.api.model.IRestResponse;
import us.careydevelopment.util.api.response.ResponseEntityUtil;

@RestController
public class UserController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    //注入service
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserFileUtil fileUtil;

    @Autowired
    private SessionUtil sessionUtil;
    
    @Autowired
    private UserUtil userUtil;


    /**
     * 例外处理程序
     * @param ex
     * @return
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<IRestResponse<Void>> userNotFound(UserNotFoundException ex) {
        return ResponseEntityUtil.createResponseEntityWithError(ex.getMessage(),
                HttpStatus.NOT_FOUND.value());
    }

    /**
     * 用户图片
     * get请求，请求地址/{userId}/profileImage
     * @param userId
     * @return ResponseEntity
     */
    @GetMapping("/{userId}/profileImage")
    public ResponseEntity<ByteArrayResource> getProfileImage(@PathVariable String userId) {
        try {
            //根据userId获取图片路径
            Path imagePath = fileUtil.fetchProfilePhotoByUserId(userId);

            //判断路径是否为空，为显示未找到，不为空显示图片路径
            if (imagePath != null) {
                LOG.debug("Getting image from " + imagePath.toString());

                ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(imagePath));

                return ResponseEntity.ok().contentLength(imagePath.toFile().length()).contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            } else {
                LOG.debug("Profile photo not found for user " + userId);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 保存图片
     * post请求，请求地址profileImage
     * @param file
     * @return ResponseEntity
     */
    @PostMapping("/profileImage")
    public ResponseEntity<IRestResponse<Void>> saveProfileImage(@RequestParam("file") MultipartFile file) {
        //获取当前登录用户
        User user = sessionUtil.getCurrentUser();
        LOG.debug("User uploading is " + user);

        //TODO: Use exception handler here
        try {
            //对图片进行保存
            fileUtil.saveProfilePhoto(file, user);

            return ResponseEntityUtil.createSuccessfulResponseEntity("Profile image created successfully!", HttpStatus.CREATED.value());
        } catch (FileTooLargeException fe) {
            //文件过大
            return ResponseEntityUtil.createResponseEntityWithError("File too large", HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (MissingFileException me) {
            //文件丢失
            return ResponseEntityUtil.createResponseEntityWithError("Missing file", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            //未知问题
            return ResponseEntityUtil.createResponseEntityWithError("Unexpected problem", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 修改资源
     * put请求，请求地址/userId
     * @param userId  该参数使用@PathVariable注解，{userId}占位符可通过注解绑定到操作方法的参数中
     * @param user
     * @param bindingResult 绑定状态
     * @return ResponseEntity
     */
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable String userId, @Valid @RequestBody User user,
            BindingResult bindingResult) {
        
        //ensure id in URL matches id in body 确保URL中的id与正文中的id匹配
        user.setId(userId);
        //日志
        LOG.debug("updated user data is " + user);        

        //验证用户
        userUtil.validateUserUpdate(user, bindingResult);
        //todo
        InputSanitizer.sanitizeBasic(user);

        //更新操作
        User updatedUser = userService.updateUser(user);
        LOG.debug("updated user is " + updatedUser);

        return ResponseEntityUtil.createSuccessfulResponseEntity("User updated successfully", HttpStatus.OK.value(), updatedUser);    
    }

    /**
     * 删除资源
     * delete请求，请求地址/userId
     * @param userId
     * @return ResponseEntity
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        LOG.debug("deleting user " + userId);
        //根据id获取删除用户
        User user = userUtil.validateUserDelete(userId);

        //删除操作
        userRepository.delete(user);
        //返回删除成功信息
        return ResponseEntityUtil.createSuccessfulResponseEntity("User successfully deleted", HttpStatus.NO_CONTENT.value());
    }

    /**
     *
     * @return ResponseEntity
     */
    @GetMapping("/me")
    public ResponseEntity<?> me() {
        try {
            //获取当前用户
            User user = sessionUtil.getCurrentUser();
            return ResponseEntityUtil.createSuccessfulResponseEntity("User successfully retrieved", HttpStatus.OK.value(), user);
        } catch (Exception e) {
            //检索当前用户时出现问题,异常信息写入日志
            LOG.error("Problem retrieving current user!", e);
            
            //intentionally vague here for security reasons   出于安全考虑，这里故意含糊其辞
            return ResponseEntityUtil.createResponseEntityWithError("User not found", HttpStatus.NOT_FOUND.value());
        }
    }

    /**
     * 退出
     * @param jwtToken
     * @param response
     * @return ResponseEntity
     */
    @DeleteMapping("/session")
    public ResponseEntity<?> logout(
            @CookieValue(name = CookieConstants.ACCESS_TOKEN_COOKIE_NAME, required = false) String jwtToken,
            HttpServletResponse response) {

        //如果token不为空将cookie失效
        if (jwtToken != null) {
            CookieUtil.expireCookie(CookieConstants.ACCESS_TOKEN_COOKIE_NAME, response);
        }

        //返回成功退出信息
        return ResponseEntityUtil.createSuccessfulResponseEntity("User successfully logged out", HttpStatus.OK.value());
    }

    /**
     * 查询
     * @param emailAddress
     * @param username
     * @return ResponseEntity
     */
    @GetMapping("/simpleSearch")
    public ResponseEntity<?> search(@RequestParam(required = false) String emailAddress,
            @RequestParam(required = false) String username) {
        //创建搜索标准封装，将搜索条件添加
        UserSearchCriteria searchCriteria = new UserSearchCriteria();
        searchCriteria.setEmailAddress(emailAddress);
        searchCriteria.setUsername(username);

        //日志输出搜索标准
        LOG.debug("Search criteria is " + searchCriteria);

        //搜索操作
        List<User> users = userService.search(searchCriteria);
        LOG.debug("Returning users " + users);

        //返回搜索信息
        return ResponseEntityUtil.createSuccessfulResponseEntity("Successful query", HttpStatus.OK.value(), users);
    }
}