package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;

import java.util.List;

public interface UserService {

   List<UserAddress> getUserAddressList(String userid);

    UserInfo login(UserInfo userInfoParam);

    UserAddress getUserAddressById(String deliveryAddressId);
}
