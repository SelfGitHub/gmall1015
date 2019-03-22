package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotation.LoginRequire;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.enums.PaymentWay;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
public class OrderController {
    @Reference
    private CartService cartService;
    @Reference
    private UserService userService;
    @Reference
    private OrderService orderService;
    /**
     * 提交订单
     *
     * @return
     */
    @LoginRequire(isNeededSuccess = true)
    @RequestMapping("toTrade")
    public String toTrade(HttpServletRequest request, Model model) {
        String userId = (String)request.getAttribute("userId");
        //获取选中的商品列表
        List<CartInfo> cartInfos = cartService.getCartByUserId(userId);
        //获取用户地址列表
        List<UserAddress> userAddressList = userService.getUserAddressList(userId);
        //根据选中的商品封装订单详情
        ArrayList<OrderDetail> orderDetailList = new ArrayList<>();
        for (CartInfo cartInfo : cartInfos) {
            if (cartInfo.getIsChecked().equals("1")){
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setImgUrl(cartInfo.getImgUrl());
                orderDetail.setSkuId(cartInfo.getSkuId());
                orderDetail.setSkuName(cartInfo.getSkuName());
                orderDetail.setOrderPrice(cartInfo.getCartPrice());
                orderDetail.setSkuNum(cartInfo.getSkuNum());
                orderDetailList.add(orderDetail);
            }
        }
        //生成交易码放入缓存中
       String tradeCode = orderService.genTradeCode(userId);
        model.addAttribute("tradeCode",tradeCode);
        model.addAttribute("userAddressList", userAddressList);
        model.addAttribute("orderDetailList", orderDetailList);
        model.addAttribute("totalAmount", getCartTotalPrice(cartInfos));
        return "trade";
    }
        @RequestMapping("submitOrder")
        @LoginRequire(isNeededSuccess = true)
        public String submitOrder(HttpServletRequest request,String tradeCode,String deliveryAddress,Model model){

            String userId = (String)request.getAttribute("userId");
            //验证交易码
            boolean bool = orderService.checkTradeCode(tradeCode,userId);
            //生成订单号
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd:HH:mm:ss");
            String format = simpleDateFormat.format(new Date());
            String outTradeNo = "atguigu"+format+System.currentTimeMillis();
            //获取购物车信息
            List<CartInfo> cartByUserId = cartService.getCartByUserId(userId);
            BigDecimal totalAmount = getCartTotalPrice(cartByUserId);
            if (bool) {
                //获取收货人地址
                UserAddress userAddress = userService.getUserAddressById(deliveryAddress);
                //封装订单数据
                List<String> delCarts = new ArrayList<>();
                OrderInfo orderInfo = new OrderInfo();
                orderInfo.setConsignee(userAddress.getConsignee());
                orderInfo.setCreateTime(new Date());
                orderInfo.setDeliveryAddress(userAddress.getUserAddress());
                orderInfo.setOrderStatus("订单已提交");
                orderInfo.setProcessStatus("订单已提交");

                orderInfo.setOutTradeNo(outTradeNo);
                orderInfo.setUserId(userId);
                orderInfo.setConsigneeTel(userAddress.getPhoneNum());
                orderInfo.setTotalAmount(totalAmount);
                //过期时间
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE,1);
                orderInfo.setExpireTime(calendar.getTime());
                orderInfo.setOrderComment("硅谷订单");
                orderInfo.setPaymentWay(PaymentWay.ONLINE);
                //封装订单详情
                List<OrderDetail> orderDertails = new ArrayList<>();
                for (CartInfo cartInfo : cartByUserId) {
                    if (cartInfo.getIsChecked().equals("1")){
                        OrderDetail orderDetail = new OrderDetail();
                        orderDetail.setSkuNum(cartInfo.getSkuNum());
                        orderDetail.setSkuName(cartInfo.getSkuName());
                        orderDetail.setOrderPrice(cartInfo.getCartPrice());
                        orderDetail.setImgUrl(cartInfo.getImgUrl());
                        orderDetail.setSkuId(cartInfo.getSkuId());
                        orderDetail.getHasStock();
                        orderDertails.add(orderDetail);
                        delCarts.add(cartInfo.getId());
                    }
                }
                orderInfo.setOrderDetailList(orderDertails);
                orderService.saveOrder(orderInfo);
                //删除购物车
                //cartService.delCartByIds(delCarts,userId);
            }else {
                return "tradeFail";

            }
            return "redirect:http://payment.gmall.com:8090/index?outTradeNo="+outTradeNo+"&totalAmount="+totalAmount;
        }

    /**
     * 获取订单的总价
     * @param orderDetailList
     * @return
     */
    private BigDecimal getCartTotalPrice(List<CartInfo> orderDetailList) {
        BigDecimal totalAmount = new BigDecimal("0");
        for (CartInfo cartInfo : orderDetailList) {
            if (cartInfo.getIsChecked().equals("1")) {
                totalAmount = totalAmount.add(cartInfo.getCartPrice());
            }
        }
        return totalAmount;
    }
}
