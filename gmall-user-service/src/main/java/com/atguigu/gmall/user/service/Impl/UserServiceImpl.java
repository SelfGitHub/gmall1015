package com.atguigu.gmall.user.service.Impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private UserAddressMapper userAddressMapper;




    @Override
    public List<UserAddress> getUserAddressList(String userid) {
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userid);
        return userAddressMapper.select(userAddress);
    }

    @Override
    public UserInfo login(UserInfo userInfoParam) {
        //如果查询出多条数据处理异常
        UserInfo userInfo = userInfoMapper.selectOne(userInfoParam);

        Jedis jedis = redisUtil.getJedis();
        //将用户信息写入缓存
        if (userInfo != null) {
            try {
                jedis.setex("user:" + userInfo.getId() + ":info", 60 * 60, JSON.toJSONString(userInfo));
            } finally {
                jedis.close();
            }
        }
        return userInfo;
    }

    @Override
    public UserAddress getUserAddressById(String deliveryAddress) {
        UserAddress userAddress = new UserAddress();
        userAddress.setId(deliveryAddress);
        UserAddress userAddressDb = userAddressMapper.selectOne(userAddress);
        return userAddressDb;
    }
}
