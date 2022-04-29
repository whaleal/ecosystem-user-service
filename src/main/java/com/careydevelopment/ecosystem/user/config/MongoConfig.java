package com.careydevelopment.ecosystem.user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

/**
 * 配置mongo
 */
@Configuration
@EnableCaching
@EnableMongoRepositories(basePackages = { "com.careydevelopment.ecosystem.user.repository" })
public class MongoConfig extends AbstractMongoClientConfiguration {

    //配置连接连接名
    @Value("${mongo.db.name}")
    private String ecosystemDb;

    //配置连接字符
    @Value("${mongodb.carey-ecosystem.connection}")
    private String connectionString;

    @Override
    protected String getDatabaseName() {
        return ecosystemDb;
    }

    /**
     * 重写mongoClient
     * @return MongoClient
     */
    @Override
    @Bean
    public MongoClient mongoClient() {
        String fullConnectionString = connectionString + "/" + ecosystemDb;

        MongoClient client = MongoClients.create(fullConnectionString);
        return client;
    }

}
