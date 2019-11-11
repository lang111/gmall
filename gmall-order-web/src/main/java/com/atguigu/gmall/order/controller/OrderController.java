package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.bean.enums.OrderStatus;
import com.atguigu.gmall.bean.enums.ProcessStatus;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageService;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class OrderController {
    //@Autowired
    @Reference
    private UserInfoService userInfoService;
    @Reference
    private CartService cartService;
    @Reference
    private OrderService orderService;
    @Reference
    private ManageService manageService;


    @RequestMapping("trade")
    //@ResponseBody
    @LoginRequire
    public String trade(HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        List<UserAddress> userAddressList = this.userInfoService.getUserAddressByUserId(userId);
        request.setAttribute("userAddressList",userAddressList);

        List<CartInfo> cartCheckedList = this.cartService.getCartCheckedList(userId);
        List<OrderDetail> orderDetailList = new ArrayList<>();
        if(cartCheckedList!=null&&cartCheckedList.size()>0){
            for (CartInfo cartInfo : cartCheckedList) {
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setImgUrl(cartInfo.getImgUrl());
                orderDetail.setSkuName(cartInfo.getSkuName());
                orderDetail.setSkuNum(cartInfo.getSkuNum());
                orderDetail.setSkuId(cartInfo.getSkuId());
                orderDetail.setOrderPrice(cartInfo.getSkuPrice());
                orderDetailList.add(orderDetail);
            }
        }
        request.setAttribute("OrderDetailList",orderDetailList);
        System.out.println("OrderDetailList===========>"+orderDetailList);
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);
        orderInfo.sumTotalAmount();
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());
        System.out.println("totalAmount=================>"+orderInfo.getTotalAmount());
        String tradeNo = this.orderService.getTradeNo(userId);
        request.setAttribute("tradeNo",tradeNo);
        return "trade";
    }

    @RequestMapping("submitOrder")
    @LoginRequire
    public String submitOrder(OrderInfo orderInfo,HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        String tradeNo = request.getParameter("tradeNo");
        boolean result = this.orderService.checkTradeCode(userId, tradeNo);
        if(!result){
            request.setAttribute("errMsg","该页面已失效，请重新结算!");
            return "tradeFail";
        }
        // 初始化参数
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        orderInfo.sumTotalAmount();
        orderInfo.setUserId(userId);

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        //验证库存信息
        for (OrderDetail orderDetail : orderDetailList) {
            String skuId = orderDetail.getSkuId();
            Integer skuNum = orderDetail.getSkuNum();

            boolean res = this.orderService.checkStock(skuId,skuNum);
            if(!res){
                request.setAttribute("errMsg", "商品"+orderDetail.getSkuName()+"库存不足，请重新下单！");
                return "tradeFail";
            }

            //验证价格信息
            SkuInfo skuInfo = this.manageService.getSkuInfo(skuId);
            int flag = skuInfo.getPrice().compareTo(orderDetail.getOrderPrice());
            if(flag!=0){
                request.setAttribute("errMsg", "商品"+orderDetail.getSkuName()+"价格有变动，请重新下单！");
                this.cartService.loadCartCache(userId);
                return "tradeFail";
            }

        }

        String orderId = this.orderService.saveOrder(orderInfo);

        this.orderService.delTradeCode(userId);

        // 重定向
        return "redirect://payment.gmall.com/index?orderId="+orderId;

    }
}
