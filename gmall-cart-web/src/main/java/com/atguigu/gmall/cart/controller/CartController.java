package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CartController {
    @Reference
    private CartService cartService;
    @Autowired
    private CartCookieHandler cartCookieHandler;
    @Reference
    private ManageService manageService;

    @RequestMapping("addToCart")
    @LoginRequire(autoRedirect = false)
    public String toCart(HttpServletRequest request, HttpServletResponse response){
        String skuId = request.getParameter("skuId");
        String skuNum =request.getParameter("skuNum");
        String userId = (String) request.getAttribute("userId");
        System.out.println("==========skuNum:"+skuNum);
        if(userId!=null){
            this.cartService.addToCart(skuId, userId, Integer.parseInt(skuNum));
        }else{

            this.cartCookieHandler.addToCart(request,response,skuId,skuNum,userId);
        }
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);

        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum", skuNum);
        request.setAttribute("userId", userId);

        return "success";
    }

    @RequestMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public String cartList(HttpServletRequest request,HttpServletResponse response){
        //判断是否有登录
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartInfoList = new ArrayList<>();
        if(userId!=null){
            //true：redis-》mysql
            List<CartInfo> cartListFromCookie  = this.cartCookieHandler.getCartList(request);

            if(cartListFromCookie !=null&&cartListFromCookie .size()>0){

               cartInfoList = this.cartService.mergeToCartList(cartListFromCookie,userId);
               this.cartCookieHandler.deleteCartCookie(request,response);
            }else{
                cartInfoList  = this.cartService.getCartList(userId);
            }

        }else{
            //false：redis-》cookie
            cartInfoList = this.cartCookieHandler.getCartList(request);
        }

        request.setAttribute("cartInfoList",cartInfoList);
        return "cartList";
    }

    @RequestMapping("checkCart")
    @ResponseBody
    @LoginRequire(autoRedirect = false)
    public void checkCart(HttpServletRequest request,HttpServletResponse response){
        String isChecked = request.getParameter("isChecked");
        String skuId = request.getParameter("skuId");
        String userId = (String) request.getAttribute("userId");

        if(userId!=null){
            this.cartService.checkCart(skuId,userId,isChecked);
        }else{
            this.cartCookieHandler.checkCart(request,response,skuId,isChecked);
        }
    }

    @RequestMapping("toTrade")
    @LoginRequire
    public String toTrade(HttpServletRequest request,HttpServletResponse response){
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cookieHandlerCartList = cartCookieHandler.getCartList(request);
        if(cookieHandlerCartList!=null&&cookieHandlerCartList.size()>0){

            this.cartService.mergeToCartList(cookieHandlerCartList,userId);
            this.cartCookieHandler.deleteCartCookie(request, response);
        }
        return "redirect://trade.gmall.com/trade";
    }
}
