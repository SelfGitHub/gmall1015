package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.CartInfo;

import java.util.List;

public interface CartService {


    CartInfo if_cart_exists(CartInfo cartInfo);

    void insertCart(CartInfo cartInfo);

    void updataCart(CartInfo cartInfo);

    void putCartCache(String userId);

    List<CartInfo> getCartByUserId(String userId);

    void changeCart(CartInfo cartInfo);

    void uniteCart(String cookieCart, String userId);

    void delCartByIds(List<String> delCarts,String userId);
}
