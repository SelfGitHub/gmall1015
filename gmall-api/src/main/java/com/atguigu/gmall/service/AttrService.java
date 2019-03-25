package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.BaseAttrValue;

import java.util.List;
import java.util.Set;

    public interface AttrService {


        List<BaseAttrInfo> getAttrList(String catalog3Id);

        void saveAttrInfo(BaseAttrInfo baseAttrInfo);

        List<BaseAttrInfo> getListAttrValueByCatalog3Id(String catalog3Id);

        List<BaseAttrInfo> getValueNameByValueIds(Set<String> valueIds);
    }
