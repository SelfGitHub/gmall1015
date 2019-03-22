package com.atguigu.gmall.manage.mapper;

import com.atguigu.gmall.bean.BaseAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrValueMapper extends Mapper<BaseAttrValue>{
    List<BaseAttrValue> selectValueNameByValueIds(String valueIdStr);
}
