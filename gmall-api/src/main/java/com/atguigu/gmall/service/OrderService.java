package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OrderInfo;

import java.util.List;

public interface OrderService {
    String genTradeCode(String userId);

    boolean checkTradeCode(String tradeCode, String userId);

    void saveOrder(OrderInfo orderInfo);

    OrderInfo getOrderByoutTradeNo(String outTradeNo);

    void updateOrder(String out_trade_no, String tracking_no);

    void sendOrderResult(String out_trade_no);
}
