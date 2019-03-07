package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;

import java.util.List;

public interface UserService {
   List<UserInfo> getAllUser();

   List<UserInfo> getUsers();

   UserInfo getUser(String loginName,String passwd);

   List<UserAddress> getUserAddressList(String userid);

}
