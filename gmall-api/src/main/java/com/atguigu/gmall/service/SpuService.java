package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.BaseSaleAttr;
import com.atguigu.gmall.bean.SpuImage;
import com.atguigu.gmall.bean.SpuInfo;

import java.util.List;

public interface SpuService {
    List<SpuInfo> supList(String catalog3Id);

    List<BaseSaleAttr> baseSaleAttrList();

    void saveSpu(SpuInfo spuInfo);

    List<SpuImage> spuImageList(String spuId);
}
