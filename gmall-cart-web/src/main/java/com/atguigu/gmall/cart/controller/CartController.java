package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.CookieUtil;
import com.atguigu.gmall.annotation.LoginRequire;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.SkuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CartController {
    @Reference
    private SkuService skuService;
    @Reference
    private CartService cartService;

    @LoginRequire(isNeededSuccess = false)
    @RequestMapping("addToCart")
    public String addToCart(HttpServletRequest request, HttpServletResponse response, String skuId, int num) {
        List<CartInfo> cartInfos = new ArrayList<>();
        CartInfo cartInfo = new CartInfo();
        SkuInfo skuInfo = skuService.getSkuById(skuId);
        cartInfo.setSkuId(skuId);
        cartInfo.setCartPrice(skuInfo.getPrice().multiply(new BigDecimal(num)));
        cartInfo.setSkuPrice(skuInfo.getPrice());
        cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
        cartInfo.setIsChecked("1");
        cartInfo.setSkuName(skuInfo.getSkuName());
        cartInfo.setSkuNum(num);
        String userId = (String) request.getAttribute("userId");
        //判断用户是否登录
        if (StringUtils.isBlank(userId)) {
            //如果为空则获取cookie
            String cookieValue = CookieUtil.getCookieValue(request, "cartListCookie", true);

            //判断cookie是否需为空,若为空直接添加否则再判断
            if (StringUtils.isNotBlank(cookieValue)) {
                cartInfos = JSON.parseArray(cookieValue, CartInfo.class);
                boolean flag = if_new_cart(cartInfos, skuId);
                //判断购物车中是否存该商品
                if (!flag) {
                    //如果为true则购物车不存在此商品直接添加
                    //将商品添加到购物车中
                    cartInfos.add(cartInfo);
                } else {
                    //更新
                    for (CartInfo info : cartInfos) {
                        if (info.getSkuId().equals(skuId)) {
                            info.setSkuNum(info.getSkuNum() + num);
                            info.setCartPrice(info.getSkuPrice().multiply(new BigDecimal(info.getSkuNum())));
                        }
                    }
                }
            } else {
                //没有cookie信息直接添加
                cartInfos.add(cartInfo);
            }
            //同步cookie
            CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(cartInfos), 24 * 60, true);
        } else {
            //如果用户登录后
            //判断该用户的购物车中是否有要添加的商品
            cartInfo.setUserId(userId);
            CartInfo cartInfoDb = cartService.if_cart_exists(cartInfo);
            if (cartInfoDb == null) {
                //添加
                cartService.insertCart(cartInfo);
            } else {
                //更新购物车
                cartInfo.setSkuNum(cartInfoDb.getSkuNum() + num);
                cartInfo.setCartPrice(cartInfo.getSkuPrice().multiply(new BigDecimal(cartInfo.getSkuNum())));
                cartService.updataCart(cartInfo);
            }
            //同步缓存
            cartService.putCartCache(userId);
        }
        return "redirect:success.html";
    }

    /**
     * 判断要添加的商品是否被添加过
     *
     * @param cartInfos
     * @param skuId
     * @return
     */
    private boolean if_new_cart(List<CartInfo> cartInfos, String skuId) {
        boolean flag = true;
        for (CartInfo cartInfo : cartInfos) {
            if (cartInfo.equals(skuId)) {
                flag = false;
            }
        }
        return flag;
    }

    @LoginRequire(isNeededSuccess = false)
    @RequestMapping("cartList")
    public String toTrade(HttpServletRequest request, Model model) {
        List<CartInfo> cartInfos = new ArrayList<>();
        //判断用户是否登录
        String userId = (String) request.getAttribute("userId");
        if (StringUtils.isBlank(userId)) {
            //用户未登录操作cookie中的数据
            String cookieList = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isNotBlank(cookieList)) {
                cartInfos = JSON.parseArray(cookieList, CartInfo.class);
            }
        } else {
            //操作缓存中的数据
            cartInfos = cartService.getCartByUserId(userId);
        }
        model.addAttribute("cartInfos", cartInfos);
        model.addAttribute("totalPrice", getCartTotalPrice(cartInfos));
        return "cartList";
    }

    @LoginRequire(isNeededSuccess = false)
    @RequestMapping("changeCart")
    public String changeCart(HttpServletRequest request, HttpServletResponse response, CartInfo cartInfo, Model model) {
        List<CartInfo> cartInfos = new ArrayList<>();
        String userId = (String) request.getAttribute("userId");
        if (StringUtils.isBlank(userId)) {
            //用户未登录操作cookie中的数据
            String cookieCart = CookieUtil.getCookieValue(request, "cartListCookie", true);
            cartInfos = JSON.parseArray(cookieCart, CartInfo.class);
            for (CartInfo info : cartInfos) {
                if (info.getSkuId().equals(cartInfo.getSkuId())) {
                    info.setIsChecked(cartInfo.getIsChecked());
                }
            }
            //更新cookie
            CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(cartInfos), 24 * 60, true);
        } else {
            //用户已登录操作db中的数据
            cartInfo.setUserId(userId);
            cartService.changeCart(cartInfo);
            cartInfos = cartService.getCartByUserId(userId);
        }
        model.addAttribute("cartInfos", cartInfos);
        model.addAttribute("totalPrice", getCartTotalPrice(cartInfos));
        return "cartListInner";
    }

    private Object getCartTotalPrice(List<CartInfo> cartInfos) {
        BigDecimal bigDecimal = new BigDecimal("0");
        for (CartInfo cartInfo : cartInfos) {
            //判断是否选中选中的价格相加
            if (cartInfo.getIsChecked().equals("1")) {
                BigDecimal cartPrice = cartInfo.getCartPrice();
                bigDecimal = bigDecimal.add(cartPrice);
            }
        }
        return bigDecimal;
    }


}
