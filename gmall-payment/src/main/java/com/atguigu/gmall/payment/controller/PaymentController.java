package com.atguigu.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;

import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.bean.enums.PaymentStatus;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PaymentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.alipay.api.AlipayConstants.SIGN_TYPE;
import static org.apache.catalina.manager.Constants.CHARSET;

@Controller
public class PaymentController {

    @Reference
    private PaymentService paymentService;
    @Reference
    private OrderService orderService;
    @Autowired
    private AlipayConfig alipayConfig;

    @RequestMapping("index")
    @LoginRequire
    public String index(HttpServletRequest request){
        String orderId = request.getParameter("orderId");
        request.setAttribute("orderId", orderId);
        OrderInfo orderInfo = this.orderService.getOrderInfo(orderId);
        request.setAttribute("totalAmount", orderInfo.getTotalAmount());
        return "index";
    }

    @RequestMapping("/alipay/submit")
    @ResponseBody
    public String alipaySubmit(HttpServletRequest request, HttpServletResponse response){
        String orderId = request.getParameter("orderId");

        OrderInfo orderInfo = this.orderService.getOrderInfo(orderId);
        // 保存支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject("帽子");
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        paymentInfo.setCreateTime(new Date());


        this.paymentService.savePaymentInfo(paymentInfo);

        AlipayClient alipayClient = alipayConfig.alipayClient();
        //AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE); //获得初始化的AlipayClient
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request

        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",orderInfo.getOutTradeNo());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount",orderInfo.getTotalAmount());
        map.put("subject","帽子");
        alipayRequest.setBizContent(JSON.toJSONString(map));

       /* alipayRequest.setBizContent("{" +
                "    \"out_trade_no\":\"20150320010101001\"," +
                "    \"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
                "    \"total_amount\":88.88," +
                "    \"subject\":\"Iphone6 16G\"," +
                "    \"body\":\"Iphone6 16G\"," +
                "    \"passback_params\":\"merchantBizType%3d3C%26merchantBizNo%3d2016010101111\"," +
                "    \"extend_params\":{" +
                "    \"sys_service_provider_id\":\"2088511833207846\"" +
                "    }"+
                "  }");//填充业务参数*/
        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        response.setContentType("text/html;charset=" + CHARSET);
       /* response.getWriter().write(form);//直接将完整的表单html输出到页面
        response.getWriter().flush();
        response.getWriter().close();*/

        //https://www.domain.com/CallBack/return_url?
        // out_trade_no=ATGUIGU1569306698447655&version=1.0
        // &app_id=2018020102122556&charset=utf-8
        // &sign_type=RSA2&trade_no=2019092422001480010561140829
        // &auth_app_id=2018020102122556&timestamp=2019-09-24
        // %2019:07:26
        // &seller_id=2088921750292524
        // &method=alipay.trade.page.pay.return
        // &total_amount=0.01
        // &sign=YFA5XPL8MiCA+i4oJQtgK50F2RB7GBge8KxVTN6pkE132JuVZDT6Qti9IvhnCVb/BA1lOMXY/+VHda1JxXuwzjV363rrhNtagYHTvf3yF1PR3MJTuk0kjhO6GFlWkPVh4YBzt9E3UzNtHe625s7ZgSgumRltsbgnIHH61phl7T+RGmTwrpbv57aGFgaHYBj3+KQI9QwnPzkNYgzW6YqUM6iynkLCfp1CxZCsd3W6Jol1IyMkiGmWMY+41UGk6SLP4itd61ZYQJzHaFJyCW3ZvU1Fa6uLV9VVxUlz/5CvcZnru4J3JRv6Ak5DAzsWnqqZ77fka3p1sMqWikFMy26POA==
        return form;
    }

    @RequestMapping("/alipay/callback/return")
    public String callbackReturn(){
        return "redirect:"+AlipayConfig.return_order_url;
    }

    @RequestMapping("/alipay/callback/notify")
    public String callbackNotify(@RequestParam  Map<String, String> paramsMap,HttpServletRequest request){
        //将异步通知中收到的所有参数都存放到map中
        boolean flag = false;//调用SDK验证签名
        try {
            flag = AlipaySignature.rsaCheckV1(paramsMap, AlipayConfig.alipay_public_key , "utf-8", AlipayConfig.sign_type);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(flag){
            // TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
            String trade_status = paramsMap.get("trade_status");
            String out_trade_no = paramsMap.get("out_trade_no");
            if("TRADE_SUCCESS".equals(trade_status)||"TRADE_FINISHED".equals(trade_status)){
                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setOutTradeNo(out_trade_no);
                PaymentInfo paymentInfoNew = this.paymentService.getPaymentInfo(paymentInfo);
                PaymentStatus paymentStatus = paymentInfoNew.getPaymentStatus();
                if(PaymentStatus.PAID.equals(paymentStatus)||PaymentStatus.ClOSED.equals(paymentStatus)){
                    return "failure";
                }

                paymentInfo.setCreateTime(new Date());
                paymentInfo.setPaymentStatus(PaymentStatus.PAID);
                paymentInfo.setCallbackContent(paramsMap.toString());
                this.paymentService.updataPaymentInfo(paymentInfo,out_trade_no);

                return "success";
            }
        }else{
            // TODO 验签失败则记录异常日志，并在response中返回failure.
            return "failure";
        }
        return "failure";
    }

    @RequestMapping("wx/submit")
    @ResponseBody
    public Map wxPaySubmit(String orderId) {

        Map map = this.paymentService.createNative(orderId+"","1");
        System.out.println("code_url==================>"+map.get("code_url"));
        return map;
    }


}
