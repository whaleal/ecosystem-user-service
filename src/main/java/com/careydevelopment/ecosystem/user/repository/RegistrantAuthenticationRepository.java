package com.careydevelopment.ecosystem.user.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.careydevelopment.ecosystem.user.model.RegistrantAuthentication;

/**
 * 注册人身份验证操作接口
 */
@Repository
public interface RegistrantAuthenticationRepository extends MongoRepository<RegistrantAuthentication, String> {

    //检查验证码
    @Query("{ 'username': '?0', 'time' : { $gte: ?1 }, 'type': '?2', 'code': '?3' }")
    public List<RegistrantAuthentication> codeCheck(String username, long sinceTime, String type, String code);

    // 根据时间排序按用户名查找
    public List<RegistrantAuthentication> findByUsernameOrderByTimeDesc(String username);

    //根据时间排序按用户名或类型查找
    public List<RegistrantAuthentication> findByUsernameAndTypeOrderByTimeDesc(String username, String type);
}
