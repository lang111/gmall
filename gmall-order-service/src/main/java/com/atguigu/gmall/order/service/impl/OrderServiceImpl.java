package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.config.HttpClientUtil;
import com.atguigu.gmall.config.JedisUtil;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;

import com.atguigu.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private JedisUtil jedisUtil;


    @Override
    @Transactional
    public String saveOrder(OrderInfo orderInfo) {
        // 设置创建时间
        orderInfo.setCreateTime(new Date());
        // 设置失效时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);
        orderInfo.setExpireTime(calendar.getTime());
        // 生成第三方支付编号
        String outTradeNo="ATGUIGU"+System.currentTimeMillis()+""+new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);

        this.orderInfoMapper.insertSelective(orderInfo);
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        if(orderDetailList!=null&&orderDetailList.size()>0){
            for (OrderDetail orderDetail : orderDetailList) {
                orderDetail.setOrderId(orderInfo.getId());
                this.orderDetailMapper.insertSelective(orderDetail);
            }
        }


        return orderInfo.getId();
    }

    @Override
    public String getTradeNo(String userId) {
        String tradeCodeKey = "user:"+userId+":tradeCode";
        String tradeCode = UUID.randomUUID().toString().replace("-", "");
        Jedis jedis = this.jedisUtil.getJedis();
        jedis.set(tradeCodeKey,tradeCode);
        jedis.close();
        return tradeCode;
    }

    @Override
    public boolean checkTradeCode(String userId, String tradeCode) {
        Jedis jedis = this.jedisUtil.getJedis();
        String tradeCodeKey = "user:"+userId+":tradeCode";
        String tradeCodeNo = jedis.get(tradeCodeKey);
        jedis.close();
            if(tradeCodeNo!=null&&tradeCodeNo.equals(tradeCode)){
                return true;
            }else{
                return false;
            }


    }

    @Override
    public void delTradeCode(String userId) {
        Jedis jedis = this.jedisUtil.getJedis();
        String tradeCodeKey = "user:"+userId+":tradeCode";
        jedis.del(tradeCodeKey);
        jedis.close();
    }

    @Override
    public boolean checkStock(String skuId, Integer skuNum) {
        String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuId + "&num=" + skuNum);
        return "1".equals(result);
    }

    @Override
    public OrderInfo getOrderInfo(String orderId) {
        return this.orderInfoMapper.selectByPrimaryKey(orderId);
    }
}
