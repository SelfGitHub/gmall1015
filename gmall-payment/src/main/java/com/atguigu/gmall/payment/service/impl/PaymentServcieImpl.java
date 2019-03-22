package com.atguigu.gmall.payment.service.impl;


import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

@Service
public class PaymentServcieImpl implements PaymentService {
    @Autowired
    private PaymentInfoMapper paymentInfoMapper;

    /**
     * 保存支付xinxi
     * @param paymentInfo
     */
    @Override
    public void savePayment(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);

    }

    /**
     * 更新支付信息
     * @param paymentInfoParam
     */
    @Override
    public void updatePayment(PaymentInfo paymentInfoParam) {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCallbackContent(paymentInfoParam.getCallbackContent());
        paymentInfo.setCallbackTime(paymentInfoParam.getCallbackTime());
        paymentInfo.setPaymentStatus(paymentInfoParam.getPaymentStatus());
        paymentInfo.setAlipayTradeNo(paymentInfoParam.getAlipayTradeNo());
        paymentInfo.setOutTradeNo(paymentInfoParam.getOutTradeNo());
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",paymentInfoParam.getOutTradeNo());
        paymentInfoMapper.updateByExampleSelective(paymentInfo,example);

    }
}
