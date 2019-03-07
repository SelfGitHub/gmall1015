package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.BaseSaleAttr;
import com.atguigu.gmall.bean.SpuImage;
import com.atguigu.gmall.bean.SpuInfo;
import com.atguigu.gmall.manage.util.MyFileUpload;
import com.atguigu.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class SpuController {
    @Reference
    private SpuService spuService;

    @RequestMapping("getSpuList")
    @ResponseBody
    public List<SpuInfo> getSpuList(String catalog3Id){
        return spuService.supList(catalog3Id);
    }

    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<BaseSaleAttr> baseSaleAttrList(){
        return spuService.baseSaleAttrList();
    }
    @RequestMapping("saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(SpuInfo spuInfo){
        spuService.saveSpu(spuInfo);
        return "success";
    }

    @RequestMapping("fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("file")MultipartFile multipartFile){
        String url = MyFileUpload.uploadImage(multipartFile);
        return url;
    }

    @RequestMapping("spuImageList")
    @ResponseBody
    public List<SpuImage> spuImageList(String spuId){
        return spuService.spuImageList(spuId);
    }

}
