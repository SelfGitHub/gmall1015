package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.BaseAttrValue;
import com.atguigu.gmall.manage.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.manage.mapper.BaseAttrValueMapper;
import com.atguigu.gmall.service.BaseAttrInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class BaseAtrrInfoServiceImpl implements BaseAttrInfoService {
    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;
    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
        List<BaseAttrInfo> select = null;
        if (StringUtils.isBlank(catalog3Id)){
            select = baseAttrInfoMapper.selectAll();
        }else{
            BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
            baseAttrInfo.setCatalog3Id(catalog3Id);
            select = baseAttrInfoMapper.select(baseAttrInfo);
        }
        return select;
    }

    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //保存属性名称
        baseAttrInfoMapper.insertSelective(baseAttrInfo);
        //返回主键
        String id = baseAttrInfo.getId();
        //根据主键返回策略返回主键根据主键插入属性追值
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        for (BaseAttrValue baseAttrValue : attrValueList) {
            baseAttrValue.setAttrId(id);
            baseAttrValueMapper.insertSelective(baseAttrValue);
        }
    }
}
