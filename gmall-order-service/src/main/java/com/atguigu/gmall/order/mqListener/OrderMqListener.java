package com.atguigu.gmall.order.mqListener;

import com.atguigu.gmall.service.OrderService;
import org.apache.activemq.command.ActiveMQMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class OrderMqListener {
    @Autowired
    private OrderService orderService;

    @JmsListener(containerFactory = "jmsQueueListener",destination = "PAYMENT_SUCCESS_QUEUE")
    public void consumePaymentResult(MapMessage message){
        //消费服务队列
        String out_trade_no = null;
        String tracking_no = null;

        try {
            out_trade_no = message.getString("out_trade_no");
            tracking_no= message.getString("tracking_no");
        } catch (JMSException e) {
            e.printStackTrace();
        }
        orderService.updateOrder(out_trade_no,tracking_no);
        //发送订单成功的队列
        orderService.sendOrderResult(out_trade_no);
    }

}
