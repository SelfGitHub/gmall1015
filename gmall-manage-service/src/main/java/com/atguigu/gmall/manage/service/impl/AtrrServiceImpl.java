package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.BaseAttrValue;
import com.atguigu.gmall.manage.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.manage.mapper.BaseAttrValueMapper;
import com.atguigu.gmall.service.AttrService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Set;

@Service
public class AtrrServiceImpl implements AttrService {
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

    @Override
    public List<BaseAttrInfo> getListAttrValueByCatalog3Id(String catalog3Id) {
        //查询平台属性
        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);
        List<BaseAttrInfo> BaseAttrList = baseAttrInfoMapper.select(baseAttrInfo);
        for (BaseAttrInfo attrInfo : BaseAttrList) {
           //根据平台属性的id查询平台属性的值List
            BaseAttrValue baseAttrValue = new BaseAttrValue();
            baseAttrValue.setAttrId(attrInfo.getId());
            List<BaseAttrValue> BaseAttrValueList = baseAttrValueMapper.select(baseAttrValue);
            attrInfo.setAttrValueList(BaseAttrValueList);
        }
        return BaseAttrList;
    }

    @Override
    public List<BaseAttrInfo> getValueNameByValueIds(Set<String> valueIds) {
        //将集合用逗号分开
        String joinStr = StringUtils.join(valueIds, ",");
        List<BaseAttrInfo> baseAttrInfos = baseAttrInfoMapper.selectValueNameByValueIds(joinStr);
        return baseAttrInfos;

    }
}
