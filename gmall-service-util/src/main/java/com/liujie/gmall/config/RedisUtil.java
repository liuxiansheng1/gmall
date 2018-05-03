package com.liujie.gmall.config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {

    private JedisPool jedisPool;
    int timeout = 5*1000;

    public void initPool(String host,int port){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(200);
        jedisPoolConfig.setMinIdle(10);
        jedisPoolConfig.setMaxIdle(20);
        jedisPoolConfig.setBlockWhenExhausted(true);
        jedisPoolConfig.setMaxWaitMillis(100000);
        jedisPoolConfig.setTestOnBorrow(true);

        jedisPool = new JedisPool(jedisPoolConfig,host,port,timeout);
    }

    public Jedis getJedis(){
        Jedis jedis = jedisPool.getResource();
        return jedis;
    }
}
