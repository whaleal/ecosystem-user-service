package com.careydevelopment.ecosystem.user.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import com.careydevelopment.ecosystem.user.model.IpLog;
import com.careydevelopment.ecosystem.user.repository.IpLogRepository;

import us.careydevelopment.ecosystem.jwt.model.IpTracker;

/**
 * id日志服务层
 */
@Service
public class IpLogService implements IpTracker {

    private static final Logger LOG = LoggerFactory.getLogger(IpLogService.class);

    //mongo模板
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IpLogRepository ipLogRepository;

    /**
     * 获取Ip故障记录
     * @param ipAddress ip地址
     * @param startingTime 开始时间
     * @return List
     */
    @Override
    public List<IpLog> fetchIpFailureRecord(String ipAddress, Long startingTime) {
        //创建list
        List<IpLog> list = new ArrayList<>();
        List<AggregationOperation> ops = new ArrayList<>();

        //ip地址不为空时进行以下操作，为空直接返回空list
        if (ipAddress != null) {

            //todo
            AggregationOperation ipMatch = Aggregation.match(Criteria.where("ipAddress").is(ipAddress));
            ops.add(ipMatch);

            AggregationOperation dateThreshold = Aggregation
                    .match(Criteria.where("lastLoginAttempt").gte(startingTime));
            ops.add(dateThreshold);

            AggregationOperation failMatch = Aggregation.match(Criteria.where("successfulLogin").is(false));
            ops.add(failMatch);

            Aggregation aggregation = Aggregation.newAggregation(ops);

            list = mongoTemplate.aggregate(aggregation, mongoTemplate.getCollectionName(IpLog.class), IpLog.class)
                    .getMappedResults();
        }

        return list;
    }

    /**
     * 登陆成功操作
     * @param username 用户名
     * @param ipAddress ip地址
     */
    @Override
    public void successfulLogin(String username, String ipAddress) {
        IpLog ipLog = new IpLog();

        //设置ipLog
        ipLog.setIpAddress(ipAddress);
        ipLog.setLastLoginAttempt(System.currentTimeMillis());
        ipLog.setSuccessfulLogin(true);
        ipLog.setUsername(username);

        //保存
        ipLogRepository.save(ipLog);
    }

    /**
     * 成功登出操作
     * @param username 用户名
     * @param ipAddress ip地址
     */
    @Override
    public void unsuccessfulLogin(String username, String ipAddress) {
        IpLog ipLog = new IpLog();

        //设置ipLog内容
        ipLog.setIpAddress(ipAddress);
        ipLog.setLastLoginAttempt(System.currentTimeMillis());
        ipLog.setSuccessfulLogin(false);
        ipLog.setUsername(username);

        //保存
        ipLogRepository.save(ipLog);
    }

}
