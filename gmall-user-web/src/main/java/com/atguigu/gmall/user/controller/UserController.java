package com.atguigu.gmall.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
@RestController
public class UserController {
    @Reference
    private UserService userService;
    
@RequestMapping("get/user/address/list")
    public List<UserAddress> getUserAddressList(@RequestParam("userid") String userid){
        return userService.getUserAddressList(userid);
    }
}
