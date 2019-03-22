package com.atguigu.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.AttrService;
import com.atguigu.gmall.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
public class SearchController {
    @Reference
    private SearchService searchService;
    @Reference
    private AttrService attrService;

    @RequestMapping("list.html")
    public String index(SkuLsParam skuLsParam,//封装页面输入的参数(关键字或三级分类Id)
                        Model model) {
        //根据关键字或三级分类ID所有满足条件的商品展示在页面
        List<SkuLsInfo> skuLsInfoList = searchService.search(skuLsParam);
        //根据商品提取所有用来检索的销售属性的值
        Set<String> valueIds = new HashSet<>();
        //遍历所有的商品将商品的ID放入set集合中可以过滤掉所有的重复id
        for (SkuLsInfo skuLsInfo : skuLsInfoList) {
            List<SkuLsAttrValue> skuAttrValueList = skuLsInfo.getSkuAttrValueList();
            for (SkuLsAttrValue skuLsAttrValue : skuAttrValueList) {
                valueIds.add(skuLsAttrValue.getValueId());
            }
        }
            //根据id的集合查询所有的平台属性值展示在页面
            List<BaseAttrInfo> attrList = attrService.getValueNameByValueIds(valueIds);
        //制作面包屑
        ArrayList<Crumb> crumbArrayList = new ArrayList<>();
        //面包屑的ID
        String[] valueIdFromCrumb = skuLsParam.getValueId();
        //如果面包屑的数组分空遍历面包屑的数组
        if (valueIdFromCrumb != null && valueIdFromCrumb.length > 0) {
            for (String fromCrumb : valueIdFromCrumb) {
                //创建面包屑的对象
                Crumb crumb = new Crumb();
                //遍历所有的平台属性
                for (BaseAttrInfo baseAttrInfo : attrList) {
                    //获取平台属性值得集合遍历
                    List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                    for (BaseAttrValue baseAttrValue : attrValueList) {
                        String id = baseAttrValue.getId();
                        //判断如果平台属性值得id和面包屑的ID相同就将属性值放入面包屑对象中
                        if (id.equals(fromCrumb)) {
                            String valueName = baseAttrValue.getValueName();
                            crumb.setValueName(valueName);
                        }
                    }
                }
                //设置面包屑的url地址()
                String myCrumbUrlParam = MyCrumbUrlParam(skuLsParam, valueIdFromCrumb);
                crumb.setUrlParam(myCrumbUrlParam);
                crumbArrayList.add(crumb);
            }
        }
        //移除选中的属性
        String[] valueIdParam = skuLsParam.getValueId();
        if (valueIdParam != null) {
            //获取迭代器对象(不能用集合否则会报下角标越界的异常)
            Iterator<BaseAttrInfo> iterator = attrList.iterator();
            while (iterator.hasNext()) {
                //获取下一个元素
                List<BaseAttrValue> attrValueList = iterator.next().getAttrValueList();
                for (BaseAttrValue baseAttrValue : attrValueList) {
                    for (String fromParam : valueIdParam) {
                        //如果列表中的valueid和选中的valueId 相等删除
                        if (fromParam.equals(baseAttrValue.getId())) {
                            iterator.remove();
                        }
                    }
                }
            }
        }
        //拼接url
        String urlParam = MyCrumbUrlParam(skuLsParam);
        model.addAttribute("crumbArrayList", crumbArrayList);
        model.addAttribute("attrList", attrList);
        model.addAttribute("urlParam", urlParam);
        model.addAttribute("skuLsInfoList", skuLsInfoList);
        return "list";
    }

    /**
     * 拼接url
     *
     * @param skuLsParam
     * @param valueIdFromCrumb
     * @return
     */
    private String MyCrumbUrlParam(SkuLsParam skuLsParam, String... valueIdFromCrumb) {
        String urlParam = "";
        String catalog3Id = skuLsParam.getCatalog3Id();
        String keyword = skuLsParam.getKeyword();
        String[] valueIds = skuLsParam.getValueId();
        //如果三级分类非空
        if (StringUtils.isNotBlank(catalog3Id)) {
            //urlParam不为空前面加&如果为空不加
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }
        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword" + keyword;
        }
        if (valueIds != null) {
            for (String valueId : valueIds) {
                if (valueIdFromCrumb.length == 0) {
                    urlParam = urlParam + "&" + "valueId=" + valueId;
                } else {
                    //当前面包屑的请求等于地址栏的请求减去自己的valueId
                    if (!valueId.equals(valueIdFromCrumb[0])) {
                        urlParam = urlParam + "&" + "valueId=" + valueId;
                    }
                }
            }
        }
        return urlParam;
    }
}
