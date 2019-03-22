package com.atguigu.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.annotation.LoginRequire;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

@Controller
public class PaymentController {
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private  AlipayClient alipayClient;
    @Reference
    private OrderService orderService;

    @RequestMapping("index")
    @LoginRequire
    public String paymentIndex(HttpServletRequest request, String outTradeNo, BigDecimal totalAmount, Model model){
        String userId =(String) request.getAttribute("userId");
        String nickName = (String) request.getAttribute("nickName");
        model.addAttribute("outTradeNo",outTradeNo);
        model.addAttribute("nickName",nickName);
        model.addAttribute("totalAmount",totalAmount);
        return "paymentindex";
    }

    @RequestMapping("/alipay/submit")
    @LoginRequire
    @ResponseBody
    public String alipaySubmit(String outTradeNo){

            AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
            alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);//成功后的重定向地址
            alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址
            HashMap<String, String> map = new HashMap<>();
            OrderInfo orderInfo = orderService.getOrderByoutTradeNo(outTradeNo);
            map.put("out_trade_no",outTradeNo);
            map.put("product_code","FAST_INSTANT_TRADE_PAY");
            map.put("total_amount","0.01");
            map.put("subject",orderInfo.getOrderDetailList().get(0).getSkuName());
            alipayRequest.setBizContent(JSON.toJSONString(map));
            String form="";
            try {
                form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
            } catch (AlipayApiException e) {
                e.printStackTrace();
            }
            System.err.print(form);
            //封装支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOutTradeNo(outTradeNo);
        paymentInfo.setPaymentStatus("未支付");
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject(orderInfo.getOrderDetailList().get(0).getSkuName());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentService.savePayment(paymentInfo);
        return form;

    }
    @RequestMapping("/alipay/callback/return")
    @LoginRequire
    public String alipayCallbackReturn(HttpServletRequest request){
        String trade_no = request.getParameter("trade_no");
        String app_id = request.getParameter("app_id");
        String out_trade_no = request.getParameter("out_trade_no");
        //更新支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(out_trade_no);
        paymentInfo.setAlipayTradeNo(trade_no);
        paymentInfo.setPaymentStatus("已支付");
        paymentInfo.setCallbackTime(new Date());
        //该方法可以获取url中路径后面的字符串
        String queryString = request.getQueryString();
        paymentInfo.setCallbackContent(queryString);
        paymentService.updatePayment(paymentInfo);
        return  "finish";
    }

}
