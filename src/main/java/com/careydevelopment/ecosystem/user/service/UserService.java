package com.careydevelopment.ecosystem.user.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.careydevelopment.ecosystem.user.exception.ServiceException;
import com.careydevelopment.ecosystem.user.exception.UserNotFoundException;
import com.careydevelopment.ecosystem.user.model.User;
import com.careydevelopment.ecosystem.user.model.UserSearchCriteria;
import com.careydevelopment.ecosystem.user.repository.UserRepository;

import us.careydevelopment.ecosystem.jwt.service.JwtUserDetailsService;

@Service
public class UserService extends JwtUserDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    /**
     * 注入Mongo模板类型
     */
    @Autowired
    private MongoTemplate mongoTemplate;
    
    public UserService(@Autowired UserRepository userRepository) {
        this.userDetailsRepository = userRepository;
    }

    private UserRepository getUserRepository() {
        return (UserRepository)userDetailsRepository;
    }

    /**
     * 更新用户
     * @param user
     * @return user
     */
    public User updateUser(User user) {
        //首先更新用户属性
        updateFields(user);
        
        try {
            //更新操作
            User updatedUser = getUserRepository().save(user);
            return updatedUser;
        } catch (Exception e) {
            //更新失败
            LOG.error("Problem updating user!", e);
            throw new ServiceException("Problem updating user!");
        }
    }

    /**
     * 更新用户属性
     * @param user
     * 不能更新用户的username与email address
     */
    private void updateFields(User user) {
        //根据当前登录用户id获取user表中数据
        Optional<User> currentUserOpt = getUserRepository().findById(user.getId());
        //判断是否存在
        if (currentUserOpt.isPresent()) {
            User currentUser = currentUserOpt.get();
            
            //don't let user change username or email address
            //they need be unique across the board
            //将现填写的信息进行持久层更新
            user.setUsername(currentUser.getUsername());
            user.setEmail(currentUser.getEmail());
            
            user.setPassword(currentUser.getPassword());
            user.setAuthorityNames(currentUser.getAuthorityNames());    
        } else {
            //账号未找到或不存在则异常信息提示
            throw new UserNotFoundException("No user with ID: " + user.getId());
        }
    }

    /**
     * 更新用户尝试登录次数
     * 当用户登录失败时次数加一
     * @param username
     */
    public void updateFailedLoginAttempts(String username) {
        try {
            //根据用户名获取用户信息
            UserDetails userDetails = loadUserByUsername(username);
            User user = (User) userDetails;

            //获取失败登录次数并赋值
            Integer failedLoginAttempts = user.getFailedLoginAttempts();
            if (failedLoginAttempts == null) {
                failedLoginAttempts = 1;
            } else {
                failedLoginAttempts++;
            }

            //记录次数与最后一次失败的时间
            user.setFailedLoginAttempts(failedLoginAttempts);
            user.setLastFailedLoginTime(System.currentTimeMillis());

            //数据持久化操作
            getUserRepository().save(user);
        } catch (UsernameNotFoundException e) {
            //尝试更新失败的登录尝试时出现问题
            LOG.error("Problem attempting to update failed login attempts!", e);
        }
    }

    /**
     * 成功登录
     * @param username
     */
    public void successfulLogin(String username) {
        resetFailedLoginAttempts(username);
    }

    /**
     * 根据用户名重置失败登录次数
     * @param username
     */
    private void resetFailedLoginAttempts(String username) {
        //获取用户信息
        UserDetails userDetails = loadUserByUsername(username);
        User user = (User) userDetails;

        //获取登陆失败次数，当次数不为null时，设置为null
        Integer failedLoginAttempts = user.getFailedLoginAttempts();
        if (failedLoginAttempts != null) {
            user.setFailedLoginAttempts(null);
            getUserRepository().save(user);
        }
    }

    //按条件搜索用户
    public List<User> search(UserSearchCriteria searchCriteria) {
        List<AggregationOperation> ops = new ArrayList<>();

        //判断邮箱地址与用户名是否为空，如果为空则返回空list
        if (StringUtils.isBlank(searchCriteria.getEmailAddress())
                && StringUtils.isBlank(searchCriteria.getUsername())) {
            return new ArrayList<>();
        }

        //判断邮箱地址是否为空，如果不为空则添加进list
        if (!StringUtils.isBlank(searchCriteria.getEmailAddress())) {
            AggregationOperation emailMatch = Aggregation
                    .match(Criteria.where("email").is(searchCriteria.getEmailAddress()));
            ops.add(emailMatch);
        }

        //判断用户名是否为空，如果不为空则添加进list
        if (!StringUtils.isBlank(searchCriteria.getUsername())) {
            AggregationOperation usernameMatch = Aggregation
                    .match(Criteria.where("username").is(searchCriteria.getUsername()));
            ops.add(usernameMatch);
        }

        //创建存储ops的集合
        Aggregation aggregation = Aggregation.newAggregation(ops);
        //todo
        List<User> users = mongoTemplate.aggregate(aggregation, mongoTemplate.getCollectionName(User.class), User.class)
                .getMappedResults();

        return users;
    }
} 
