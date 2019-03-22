package com.atguigu.gmall.order.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import java.util.List;
import java.util.UUID;
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private RedisUtil redisUtil;
    /**
     * 生成交易码放入缓存
     * @param userId
     * @return
     */
    @Override
    public String genTradeCode(String userId) {
        //生成交易码
        String  tradeCode = UUID.randomUUID().toString();
        String tradeCodekey = "user:"+userId+":tradeCode";
        //放入缓存
        Jedis jedis = redisUtil.getJedis();

        try {
            jedis.setex(tradeCodekey,60*30,tradeCode);
        } finally {
            jedis.close();
        }
        return tradeCode;
    }

    /**
     * 校验交易码
     * @param tradeCode
     * @param userId
     * @return
     */
    @Override
    public boolean checkTradeCode(String tradeCode, String userId) {
        boolean bool = false;
        Jedis jedis = redisUtil.getJedis();
       String tradeCodekey =  "user:" + userId + ":tradeCode";
        String tradeCodeCache = jedis.get(tradeCodekey);
        try {
            if (StringUtils.isNotBlank(tradeCodeCache)){
                if (tradeCodeCache.equals(tradeCode)){
                    bool = true;
                    //删除缓存中的交易码
                    jedis.del(tradeCodekey);
                }
            }
        } finally {
            jedis.close();
        }
        return bool;
    }

    /**
     * 保存订单数据
     * @param orderInfo
     */
    @Override
    public void saveOrder(OrderInfo orderInfo) {

        //保存订单数据
        orderInfoMapper.insertSelective(orderInfo);
        String orderId = orderInfo.getId();
        //保存订单详情
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderId);
            orderDetailMapper.insertSelective(orderDetail);
        }
    }

    @Override
    public OrderInfo getOrderByoutTradeNo(String outTradeNo) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOutTradeNo(outTradeNo);
        OrderInfo orderInfoDb = orderInfoMapper.selectOne(orderInfo);
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderInfoDb.getId());
        List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);
        orderInfoDb.setOrderDetailList(orderDetailList);
        return orderInfoDb;
    }


}
