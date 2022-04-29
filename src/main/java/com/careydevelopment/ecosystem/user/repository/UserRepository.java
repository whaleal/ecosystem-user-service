package com.careydevelopment.ecosystem.user.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.careydevelopment.ecosystem.user.model.User;

import us.careydevelopment.ecosystem.jwt.repository.UserDetailsRepository;

/**
 * 用户操作接口
 */
@Repository
public interface UserRepository extends MongoRepository<User, String>, UserDetailsRepository {

    //根据用户名查询
    public User findByUsername(String username);

    //根据email查询
    public User findByEmail(String email);

}
