package com.liujie.gmall.usermanage.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.liujie.gmall.UserAddress;
import com.liujie.gmall.UserInfo;
import com.liujie.gmall.UserService;
import com.liujie.gmall.config.RedisUtil;
import com.liujie.gmall.usermanage.mapper.UserAddressMapper;
import com.liujie.gmall.usermanage.mapper.UserInfoMapper;
import org.apache.catalina.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;


import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserAddressMapper userAddressMapper;
    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    RedisUtil redisUtil;




    public static final String USER_KEY_PREFIX = "user:";
    public static final String USER_KEY_SUFFIX = ":info";
    public static final int USER_KEY_TIMEOUT = 60*60*24*7;



    @Override
    public UserInfo verify(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String key = USER_KEY_PREFIX+userId+USER_KEY_SUFFIX;
        String userJson = jedis.get(key);
        jedis.expire(key,USER_KEY_TIMEOUT);
        jedis.close();
        if(userJson!=null){
            UserInfo userInfo = JSON.parseObject(userJson, UserInfo.class);
            return userInfo;
        }

        return null;
    }

    public void addUser(UserInfo userInfo){
        String md2Hex = DigestUtils.md2Hex(userInfo.getPasswd());
        userInfo.setPasswd(md2Hex);


        userInfoMapper.insert(userInfo);
    }

    public UserInfo login(UserInfo userInfo){
        String md2Hex = DigestUtils.md2Hex(userInfo.getPasswd());
        userInfo.setPasswd(md2Hex);

        UserInfo result = userInfoMapper.selectOne(userInfo);

        if(result!=null) {
            Jedis jedis = redisUtil.getJedis();
            String resultJson = JSON.toJSONString(result);
            jedis.setex(USER_KEY_PREFIX + result.getId() + USER_KEY_SUFFIX, USER_KEY_TIMEOUT,resultJson );
            jedis.close();
            return result ;
        }
        return null;
    }

    @Override
    public List<UserAddress> getUserAddressList(String userId) {

            UserAddress ua = new UserAddress();
            ua.setId(userId);
            List<UserAddress> select = userAddressMapper.select(ua);

            return select;
        }

}
