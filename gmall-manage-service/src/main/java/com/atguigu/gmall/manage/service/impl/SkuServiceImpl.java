package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.manage.mapper.*;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;
@Service
public class SkuServiceImpl implements SkuService {
    @Autowired
    private SkuInfoMapper skuInfoMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    private SkuImageMapper skuImageMapper;
    @Autowired
    private  RedisUtil redisUtil;

    /**
     * 查询spu下的所有sku
     * @param spuId 查询参数
     * @return 查询到的集合
     */
    @Override
    public List<SkuInfo> skuInfoListBySpu(String spuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setSpuId(spuId);
        return skuInfoMapper.select(skuInfo);
    }

    /**
     * 保存sku
     * @param skuInfo
     */

    @Override
    public void saveSku(SkuInfo skuInfo) {
        //保存 skuinfo
        skuInfoMapper.insertSelective(skuInfo);
        //保存skuAttrValue
        String skuId = skuInfo.getId();
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            skuAttrValue.setSkuId(skuId);
            skuAttrValueMapper.insertSelective(skuAttrValue);
        }
        //保存skuSaleAttrValue
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
            skuSaleAttrValue.setSkuId(skuId);
            skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
        }
        //保存图片

        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage skuImage : skuImageList) {
            skuImage.setSkuId(skuId);
            skuImageMapper.insertSelective(skuImage);
        }


    }

    /**
     * 查询sku对象
     * @param skuId
     * @return
     */
    @Override
    public SkuInfo item(String skuId) {
        SkuInfo skuInfo = null;
        Jedis jedis = redisUtil.getJedis();
        String skuStr = jedis.get("sku:" + skuId + ":info");
        skuInfo = JSON.parseObject(skuStr, SkuInfo.class);

        if (skuInfo == null){
           skuInfo = getSkuFromDb(skuId);
           //同步redis
            jedis.set("sku:"+skuId+":info",JSON.toJSONString(skuInfo));
        }else{
            return  skuInfo;
        }

        return skuInfo ;
    }

    /**
     *
     * @param skuId
     * @return
     */

    public SkuInfo getSkuFromDb(String skuId){
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        SkuInfo skuInfo1 = skuInfoMapper.selectOne(skuInfo);
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> select = skuImageMapper.select(skuImage);
        skuInfo1.setSkuImageList(select);
        return skuInfo1;
    }

    /**
     * 根据spu查询所有的sku
     * @param spuId
     * @return
     */
    @Override
    public List<SkuInfo> getSkuSaleAttrValueListBySpu(String spuId) {
        List<SkuInfo> skuInfos = skuInfoMapper.selectSkuSaleAttrValueListBySpu(spuId);
        return skuInfos;
    }

    /**
     * 查询所有的sku
     * @return
     */
    @Override
    public List<SkuInfo> getSkuInfoWithValueId() {
        List<SkuInfo> skuInfos = skuInfoMapper.selectAll();
        for (SkuInfo skuInfo : skuInfos) {
            String skuId = skuInfo.getId();
            SkuAttrValue skuAttrValue = new SkuAttrValue();
            skuAttrValue.setSkuId(skuId);
            List<SkuAttrValue> select = skuAttrValueMapper.select(skuAttrValue);
            skuInfo.setSkuAttrValueList(select);
        }
        return skuInfos;
    }

    /**
     * 查询sku
     * @param skuId
     * @return
     */
    @Override
    public SkuInfo getSkuById(String skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        return skuInfoMapper.selectOne(skuInfo);
    }


}
