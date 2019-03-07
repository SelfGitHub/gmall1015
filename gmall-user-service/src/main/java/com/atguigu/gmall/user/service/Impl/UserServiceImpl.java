package com.atguigu.gmall.user.service.Impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
@Service
public class UserServiceImpl implements UserService {

    @Autowired

    private UserInfoMapper userInfoMapper;

    @Override
    public List<UserInfo> getAllUser() {
        List<UserInfo> userInfos = userInfoMapper.selectAll();
        return userInfos;
    }

    @Override
    public List<UserInfo> getUsers() {
        return userInfoMapper.selectAllUser();
    }

    @Override
    public UserInfo getUser(String loginName, String passwd) {

        return userInfoMapper.selectUser(loginName,passwd);
    }

    @Override
    public List<UserAddress> getUserAddressList(String userid) {
        return userInfoMapper.selectUserAddressList(userid);
    }
}
