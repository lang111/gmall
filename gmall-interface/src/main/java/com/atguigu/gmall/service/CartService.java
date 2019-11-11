package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.CartInfo;

import java.util.List;

public interface CartService {

    void addToCart(String skuId, String userId, Integer skuNum);

    List<CartInfo> getCartList(String userId);

    List<CartInfo> mergeToCartList(List<CartInfo> cartListFromCookie, String userId);

    void checkCart(String skuId, String userId, String isChecked);

    List<CartInfo> getCartCheckedList(String userId);

    public List<CartInfo> loadCartCache(String userId);
}
