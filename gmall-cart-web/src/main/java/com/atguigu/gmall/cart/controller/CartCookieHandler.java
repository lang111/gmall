package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.config.CookieUtil;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class CartCookieHandler {
    // 定义购物车名称
    private String cookieCartName = "CART";
    // 设置cookie 过期时间
    private int COOKIE_CART_MAXAGE=7*24*3600;
    
    @Reference
    private ManageService manageService;


    public void addToCart(HttpServletRequest request, HttpServletResponse response, String skuId, String skuNum, String userId) {
        String cookieValue = CookieUtil.getCookieValue(request, cookieCartName, true);
        List<CartInfo> cartInfoList = new ArrayList<>();
        boolean isMeach = false;
        if(cookieValue!=null){
            cartInfoList = JSON.parseArray(cookieValue, CartInfo.class);
            for (CartInfo cartInfo : cartInfoList) {
                if(cartInfo.getSkuId().equals(skuId)){
                    cartInfo.setSkuNum(cartInfo.getSkuNum()+Integer.parseInt(skuNum));
                    // 价格设置
                    cartInfo.setSkuPrice(cartInfo.getCartPrice());
                    isMeach = true;
                }

            }
        }

        if(!isMeach){
            SkuInfo skuInfo = this.manageService.getSkuInfo(skuId);
            CartInfo cartInfo = new CartInfo();
            cartInfo.setSkuNum(Integer.parseInt(skuNum));
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuId(skuId);
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setUserId(userId);
            cartInfoList.add(cartInfo);

        }

        CookieUtil.setCookie(request, response, cookieCartName,JSON.toJSONString(cartInfoList) , COOKIE_CART_MAXAGE, true);


    }

    public List<CartInfo> getCartList(HttpServletRequest request) {
        String cookieValue = CookieUtil.getCookieValue(request, cookieCartName, true);
        List<CartInfo> cartInfoList = JSON.parseArray(cookieValue, CartInfo.class);
        return cartInfoList;

    }

    public void deleteCartCookie(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.deleteCookie(request, response, cookieCartName);
    }

    public void checkCart(HttpServletRequest request, HttpServletResponse response, String skuId, String isChecked) {
        List<CartInfo> cartInfoList = getCartList(request);
        for (CartInfo cartInfo : cartInfoList) {
            if(cartInfo.getSkuId().equals(skuId)){
                cartInfo.setIsChecked(isChecked);
            }
        }
        CookieUtil.setCookie(request, response, cookieCartName, JSON.toJSONString(cartInfoList),COOKIE_CART_MAXAGE,true);
        System.out.println("cartInfoList===============>"+JSON.toJSONString(cartInfoList));

    }
}
