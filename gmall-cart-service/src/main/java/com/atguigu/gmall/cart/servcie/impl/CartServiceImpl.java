package com.atguigu.gmall.cart.servcie.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.StringUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private CartInfoMapper cartInfoMapper;
    @Autowired
    RedisUtil redisUtil;

    /**
     * 查询该用户的购物车中是否有要添加的商品
     *
     * @param cartInfoParam
     * @return
     */
    @Override
    public CartInfo if_cart_exists(CartInfo cartInfoParam) {
        CartInfo cartInfo = new CartInfo();
        String skuId = cartInfoParam.getSkuId();
        String userId = cartInfoParam.getUserId();
        cartInfo.setUserId(userId);
        cartInfo.setSkuId(skuId);
        CartInfo cartInfoDb = cartInfoMapper.selectOne(cartInfo);
        return cartInfoDb;
    }

    @Override
    public void insertCart(CartInfo cartInfoParam) {

        cartInfoMapper.insertSelective(cartInfoParam);
    }

    @Override
    public void updataCart(CartInfo cartInfoParam) {
        CartInfo cartInfo = new CartInfo();
        cartInfo.setCartPrice(cartInfoParam.getCartPrice());
        cartInfo.setSkuNum(cartInfoParam.getSkuNum());
        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("userId", cartInfoParam.getUserId()).andEqualTo("skuId", cartInfoParam.getSkuId());
        cartInfoMapper.updateByExampleSelective(cartInfo, example);

    }

    /**
     * 同步缓存
     *
     * @param userId
     */
    @Override
    public void putCartCache(String userId) {
        Jedis jedis = redisUtil.getJedis();
        try {
            CartInfo cartInfo = new CartInfo();
            cartInfo.setUserId(userId);
            List<CartInfo> select = cartInfoMapper.select(cartInfo);
            if (select != null && select.size() > 0) {
                HashMap<String, String> map = new HashMap<>();
                for (CartInfo info : select) {
                    map.put(info.getSkuId(), JSON.toJSONString(info));
                }
                //先删除缓存中的数据在把购物车中剩余的数据同步缓存
                jedis.del("cart:" + userId + ":info");
                jedis.hmset("cart:" + userId + ":info", map);
            } else {
                //如果购物车为空直接删除缓存中的数据
                jedis.del("cart:" + userId + ":info");
            }

        } finally {
            jedis.close();
        }

    }

    @Override
    public List<CartInfo> getCartByUserId(String userId) {
        List<CartInfo> cartInfos = new ArrayList<>();
        Jedis jedis = redisUtil.getJedis();

        try {
            List<String> hvals = jedis.hvals("cart:" + userId + ":info");

            for (String hval : hvals) {
                CartInfo cartInfo = JSON.parseObject(hval, CartInfo.class);
                cartInfos.add(cartInfo);
            }
        } finally {
            jedis.close();
        }
        return cartInfos;
    }

    @Override
    public void changeCart(CartInfo cartInfo) {
        CartInfo cart = new CartInfo();
        //要更新的数据
        cart.setIsChecked(cartInfo.getIsChecked());
        //更新的条件
        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("skuId", cartInfo.getSkuId()).andEqualTo("userId", cartInfo.getUserId());
        cartInfoMapper.updateByExampleSelective(cart, example);
        //同步缓存单拿单放
        Jedis jedis = redisUtil.getJedis();

        try {
            String hget = jedis.hget("cart:" + cartInfo.getUserId() + ":info", cartInfo.getSkuId());
            CartInfo cartInfoCache = JSON.parseObject(hget, CartInfo.class);
            cartInfoCache.setIsChecked(cartInfo.getIsChecked());
            jedis.hset("cart:" + cartInfo.getUserId() + ":info", cartInfo.getSkuId(), JSON.toJSONString(cartInfoCache));
        } finally {
            jedis.close();
        }

    }

    @Override
    public void uniteCart(String cookieCart, String userId) {
        //查询购物车
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        List<CartInfo> cartListDb = cartInfoMapper.select(cartInfo);
        List<CartInfo> cartListCookie = JSON.parseArray(cookieCart, CartInfo.class);
        if (cartListCookie != null && cartListCookie.size() > 0) {
            for (CartInfo info : cartListCookie) {
                if (cartListDb != null && cartListDb.size() > 0) {
                    for (CartInfo cartInfoDb : cartListDb) {
                        //如果skuid相等更新
                        if (cartInfoDb.getSkuId().equals(info.getSkuId())) {
                            BigDecimal skuNum = new BigDecimal(info.getSkuNum()).add(new BigDecimal(cartInfoDb.getSkuNum()));
                            cartInfoDb.setSkuNum(skuNum.intValue());
                            cartInfoDb.setCartPrice(skuNum.multiply(cartInfoDb.getSkuPrice()));
                            Example example = new Example(CartInfo.class);
                            example.createCriteria().andEqualTo("userId", userId).andEqualTo("skuId", cartInfoDb.getSkuId());
                            cartInfoMapper.updateByExampleSelective(cartInfoDb, example);
                        } else {
                            //添加
                            info.setUserId(userId);
                            cartInfoMapper.insertSelective(info);
                        }
                    }
                }else{
                    //添加
                    info.setUserId(userId);
                    cartInfoMapper.insertSelective(info);
                }
            }
        }
        //同步缓存
        Jedis jedis = redisUtil.getJedis();
        try {
            HashMap<String, String> map = new HashMap<>();
            for (CartInfo info : cartListDb) {
                map.put(info.getSkuId(),JSON.toJSONString(info));
            }
            jedis.hmset("cart:"+userId+":info",map);
        } finally {
            jedis.close();
        }


    }

    @Override
    public void delCartByIds(List<String> delCarts, String userId) {
        String joinId = StringUtils.join(delCarts, ",");
        cartInfoMapper.deleteCartCheck(joinId);
        //同步缓存
        putCartCache(userId);
    }
}
