package com.careydevelopment.ecosystem.user.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.careydevelopment.ecosystem.user.model.IpLog;

/**
 * Ip日志存储接口
 */
@Repository
public interface IpLogRepository extends MongoRepository<IpLog, String> {

}
