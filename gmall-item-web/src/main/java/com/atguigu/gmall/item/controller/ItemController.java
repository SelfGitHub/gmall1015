package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {
    @Reference
    private SpuService spuService;
    @Reference
    private SkuService skuService;

    @RequestMapping("{skuId}.html")
    public String getSpuSaleAttrListCheckBySku(@PathVariable("skuId") String skuId ,Model model){
        //得到spuId
        SkuInfo item = skuService.item(skuId);
        String spuId = item.getSpuId();
        //根据skuId和spuId查询选中的
        List<SpuSaleAttr> spuSaleAttrListCheckBySku = spuService.getSpuSaleAttrListCheckBySku(spuId,skuId);
        List<SkuInfo> skuSaleAttrValueListBySpu = skuService.getSkuSaleAttrValueListBySpu(spuId);
        //将查询到的数据以哈希表的形式存放
        Map<String,String > map = new HashMap<>();
        for (SkuInfo skuInfo : skuSaleAttrValueListBySpu) {
            List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
            String k = "";
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                k = k + "|" +skuSaleAttrValue.getSaleAttrValueId();
            }
            String v = skuInfo.getId();
            map.put(k,v);
        }
        String skuMap = JSON.toJSONString(map);
        model.addAttribute("skuMap",skuMap);
        model.addAttribute("skuInfo",item);
        model.addAttribute("spuSaleAttrListCheckBySku",spuSaleAttrListCheckBySku);
        return "item";
    }


}
