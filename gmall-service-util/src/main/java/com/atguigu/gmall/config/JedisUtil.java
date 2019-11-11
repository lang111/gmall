package com.atguigu.gmall.config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisUtil {

    private JedisPool jedisPool;

    public void initJedisPool(String host,int port,int timeOut,int database){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        //设置等待队列
        jedisPoolConfig.setBlockWhenExhausted(true);
        // 最少剩余数
        jedisPoolConfig.setMinIdle(10);
        //最大连接数
        jedisPoolConfig.setMaxTotal(200);
        //设置连接尝试
        jedisPoolConfig.setTestOnBorrow(true);
        // 设置等待时间
        jedisPoolConfig.setMaxWaitMillis(10*1000);

        jedisPool = new JedisPool(jedisPoolConfig,host,port,timeOut);
    }

    public Jedis getJedis(){
        Jedis jedis = jedisPool.getResource();
        return jedis;
    }
}
