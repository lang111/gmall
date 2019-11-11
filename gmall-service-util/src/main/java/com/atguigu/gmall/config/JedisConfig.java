package com.atguigu.gmall.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JedisConfig {

    @Value("${spring.redis.host:disabled}")
    private String host;
    @Value("${spring.redis.port:0}")
    private int port;
    @Value("${spring.redis.database:0}")
    private int database;
    @Value(("${spring.redis.timeOut:10000}"))
    private int timeOut;

    @Bean
    public JedisUtil getJedisUtil(){
        if("disabled".equals(host)){
            return null;
        }
        JedisUtil jedisUtil = new JedisUtil();
        jedisUtil.initJedisPool(host, port, timeOut, database);
        return jedisUtil;
    }
}
