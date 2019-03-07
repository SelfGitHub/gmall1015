package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.manage.mapper.*;
import com.atguigu.gmall.service.SpuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class SpuServiceImpl implements SpuService {
    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private SpuInfoMapper spuInfoMapper;
    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    private SpuImageMapper spuImageMapper;

    @Override
    public List<SpuInfo> supList(String catalog3Id) {
        List<SpuInfo> list = null;
        if (StringUtils.isBlank(catalog3Id)){
            list = spuInfoMapper.selectAll();
        }else {
            SpuInfo spuInfo = new SpuInfo();
            spuInfo.setCatalog3Id(catalog3Id);
            list = spuInfoMapper.select(spuInfo);
        }
        return list;
    }

    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    @Override
    public void saveSpu(SpuInfo spuInfo) {

        spuInfoMapper.insertSelective(spuInfo);
        //保存spuSaleAttr
        String spuId = spuInfo.getId();
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();

        for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                spuSaleAttr.setSpuId(spuId);
            spuSaleAttrMapper.insertSelective(spuSaleAttr);
            ///保存spuSaleAttrValue
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {

                spuSaleAttrValue.setSpuId(spuId);
                spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
            }
        }
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();

        for (SpuImage spuImage : spuImageList) {
            spuImage.setSpuId(spuId);
            spuImageMapper.insertSelective(spuImage);
        }

    }

    @Override
    public List<SpuImage> spuImageList(String spuId) {
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        return spuImageMapper.select(spuImage);
    }
}


