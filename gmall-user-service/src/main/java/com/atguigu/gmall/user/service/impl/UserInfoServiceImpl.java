package com.atguigu.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;

import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.config.JedisUtil;
import com.atguigu.gmall.service.ManageService;
import com.atguigu.gmall.service.UserInfoService;

import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
@Service
public class UserInfoServiceImpl implements UserInfoService {
    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60*24;


    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private UserAddressMapper userAddressMapper;
    @Autowired
    private JedisUtil jedisUtil;

    @Override
    public List<UserInfo> getUserInfoList() {
        return this.userInfoMapper.selectAll();
    }

    @Override
    public List<UserAddress> getUserAddressByUserId(String userId) {
        Example example = new Example(UserAddress.class);
        example.createCriteria().andEqualTo("userId", userId);

        List<UserAddress> userAddress = this.userAddressMapper.selectByExample(example);
        return userAddress;
    }

    @Override
    public UserInfo getUserInfoByName(String name) {
        return null;
    }

    @Override
    public List<UserInfo> getUserInfoListByName(UserInfo userInfo) {
        return null;
    }

    @Override
    public List<UserInfo> getUserInfoListByNickName(UserInfo userInfo) {
        return null;
    }

    @Override
    public void addUser(UserInfo userInfo) {

    }

    @Override
    public void updUser(UserInfo userInfo) {

    }

    @Override
    public void delUser(UserInfo userInfo) {

    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        String passwd = userInfo.getPasswd();
        String newPasswd = DigestUtils.md5DigestAsHex(passwd.getBytes());
        userInfo.setPasswd(newPasswd);
        UserInfo info = this.userInfoMapper.selectOne(userInfo);
        if(info!=null){
            Jedis jedis = this.jedisUtil.getJedis();
            //userKey = user:userId:info
            String userKey = userKey_prefix + info.getId() + userinfoKey_suffix;
            jedis.setex(userKey,userKey_timeOut, JSON.toJSONString(info));
            jedis.close();
            return info;
        }
            return null;


    }

    @Override
    public UserInfo verify(String userId) {
        Jedis jedis = this.jedisUtil.getJedis();
        String key = userKey_prefix + userId + userinfoKey_suffix;
        String userJSON = jedis.get(key);
        if(!StringUtils.isEmpty(userJSON)){
            UserInfo info = JSON.parseObject(userJSON, UserInfo.class);
            return info;
        }
        return null;
    }
}
