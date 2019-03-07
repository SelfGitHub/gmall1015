package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SpuImage;

import java.util.List;

public interface SkuService {
    List<SkuInfo> skuInfoListBySpu(String spuId);


}
