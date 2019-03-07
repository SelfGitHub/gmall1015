package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.BaseAttrInfo;

import com.atguigu.gmall.service.BaseAttrInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class AttrController {

    @Reference
   private BaseAttrInfoService baseAttrInfoService;
    @RequestMapping("getAttrList")
    @ResponseBody
    public List<BaseAttrInfo> getAttrList(String catalog3Id){

        return baseAttrInfoService.getAttrList(catalog3Id);
    }
    @RequestMapping("saveAttrInfo")
    @ResponseBody
    public String saveAttrInfo(BaseAttrInfo baseAttrInfo){
        baseAttrInfoService.saveAttrInfo(baseAttrInfo);
        return "success";
    }

}
