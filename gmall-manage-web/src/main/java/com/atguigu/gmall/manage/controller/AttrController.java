package com.atguigu.gmall.manage.controller;
import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.service.AttrService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;
@Controller
public class AttrController {

    @Reference
   private AttrService attrService;
    @RequestMapping("getAttrList")
    @ResponseBody
    public List<BaseAttrInfo> getAttrList(String catalog3Id){

        return attrService.getAttrList(catalog3Id);
    }
    @RequestMapping("saveAttrInfo")
    @ResponseBody
    public String saveAttrInfo(BaseAttrInfo baseAttrInfo){
        attrService.saveAttrInfo(baseAttrInfo);
        return "success";
    }

    @RequestMapping("getListAttrValueByCatalog3Id")
    @ResponseBody
    public List<BaseAttrInfo> getListAttrValueByCatalog3Id(String catalog3Id){
        List<BaseAttrInfo> BaseAttrInfoList = attrService.getListAttrValueByCatalog3Id(catalog3Id);
        return BaseAttrInfoList;
    }

}
