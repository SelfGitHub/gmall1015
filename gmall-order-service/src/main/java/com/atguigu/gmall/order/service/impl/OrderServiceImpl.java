package com.atguigu.gmall.order.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.util.ActiveMQUtil;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
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
   @Autowired
   private ActiveMQUtil activeMQUtil;
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

    @Override
    public void updateOrder(String out_trade_no, String tracking_no) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setProcessStatus("订单已支付");
        orderInfo.setOrderStatus("订单已支付");
        orderInfo.setTrackingNo(tracking_no);
        Example example = new Example(OrderInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",out_trade_no);
        orderInfoMapper.updateByExampleSelective(orderInfo,example);

    }

    @Override
    public void sendOrderResult(String out_trade_no) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOutTradeNo(out_trade_no);
        OrderInfo orderInfoResult = orderInfoMapper.selectOne(orderInfo);
        Connection connection = activeMQUtil.getConnection();
        String orderJson = JSON.toJSONString(connection);
        try {
            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue queue = session.createQueue("ORDER_SUCCESS_QUEUE");
            MessageProducer producer = session.createProducer(queue);

            ActiveMQTextMessage message = new ActiveMQTextMessage();
            message.setText(orderJson);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(message);
            session.commit();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }


}
