package com.atguigu.gmall.service;


import com.atguigu.gmall.bean.PaymentInfo;

import java.io.IOException;
import java.util.Map;

public interface PaymentService {

    void savePaymentInfo(PaymentInfo paymentInfo);

    PaymentInfo getPaymentInfo(PaymentInfo paymentInfo);

    void updataPaymentInfo(PaymentInfo paymentInfo, String paymentInfoId);

    Map createNative(String orderId, String total_fee);
}
