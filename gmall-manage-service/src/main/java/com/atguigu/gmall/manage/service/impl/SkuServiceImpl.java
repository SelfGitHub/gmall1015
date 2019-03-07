package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SpuImage;
import com.atguigu.gmall.manage.mapper.SkuInfoMapper;
import com.atguigu.gmall.manage.mapper.SpuImageMapper;
import com.atguigu.gmall.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
@Service
public class SkuServiceImpl implements SkuService {
    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;
    @Override
    public List<SkuInfo> skuInfoListBySpu(String spuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setSpuId(spuId);
        return skuInfoMapper.select(skuInfo);
    }


}
