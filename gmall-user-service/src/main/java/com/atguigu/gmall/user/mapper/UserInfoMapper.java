package com.atguigu.gmall.user.mapper;

import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import javax.websocket.server.PathParam;
import java.util.List;

public interface UserInfoMapper extends Mapper<UserInfo> {

    List<UserInfo> selectAllUser();

    UserInfo selectUser(@Param("loginName") String loginName, @Param("passwd") String passwd);

    List<UserAddress> selectUserAddressList(String userid);
}
