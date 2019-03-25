package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.PaymentInfo;

import java.util.Map;

public interface PaymentService {

    void savePayment(PaymentInfo paymentInfo);

    void updatePayment(PaymentInfo paymentInfo);

    void sendPaymentResult(String out_trade_no, String result,String trade_no);

    void sendDelayPaymentResult(String outTradeNo,int count);

    Map<String,Object> checkAlipayPayment(String out_trade_no);

    Map<String,Object> checkAlipayPaymentFromDb(String out_trade_no);
}
