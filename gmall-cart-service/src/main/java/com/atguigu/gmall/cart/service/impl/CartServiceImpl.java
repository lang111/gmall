package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;

import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.cart.constant.CartConst;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;

import com.atguigu.gmall.config.JedisUtil;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;


@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private CartInfoMapper cartInfoMapper;
    @Autowired
    private JedisUtil jedisUtil;
    @Reference
    private ManageService manageService;

    @Override
    public void addToCart(String skuId,String userId,Integer skuNum) {
        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(userId);
        CartInfo cartInfoExist  = this.cartInfoMapper.selectOne(cartInfo);
        System.out.println("skuNum:==========="+skuNum);
        System.out.println("==========cartInfo:"+cartInfo);
        System.out.println("===============cartInfoExist:"+cartInfoExist);
        if(cartInfoExist!=null){
            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum()+skuNum);

            cartInfoExist.setSkuPrice(cartInfoExist.getCartPrice());
            this.cartInfoMapper.updateByPrimaryKeySelective(cartInfoExist);
        }else{

            SkuInfo skuInfo1 = this.manageService.getSkuInfo(skuId);
            CartInfo cartInfo1 = new CartInfo();
            cartInfo1.setSkuId(skuInfo1.getId());
            cartInfo1.setUserId(userId);
            cartInfo1.setSkuNum(skuNum);
            cartInfo1.setSkuName(skuInfo1.getSkuName());
            cartInfo1.setCartPrice(skuInfo1.getPrice());
            cartInfo1.setSkuPrice(skuInfo1.getPrice());
            cartInfo1.setImgUrl(skuInfo1.getSkuDefaultImg());

            this.cartInfoMapper.insertSelective(cartInfo1);

            cartInfoExist = cartInfo1;
        }

        Jedis jedis = this.jedisUtil.getJedis();
        String cartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        jedis.hset(cartKey,skuId, JSON.toJSONString(cartInfoExist));

        String userKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USERINFOKEY_SUFFIX;
        Long ttl = jedis.ttl(userKey);
        jedis.expire(cartKey, ttl.intValue());


        jedis.close();



    }

    @Override
    public List<CartInfo> getCartList(String userId) {
        Jedis jedis = this.jedisUtil.getJedis();
        String cartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        List<CartInfo> cartInfoList = new ArrayList<>();
        List<String> stringList = jedis.hvals(cartKey);
        if(stringList!=null&&stringList.size()>0){
            for (String cartJson : stringList) {
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                cartInfoList.add(cartInfo);
            }
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return o1.getId().compareTo(o2.getId());
                }
            });
        }else{
            cartInfoList = loadCartCache(userId);
        }

        return cartInfoList;
    }

    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cartListFromCookie, String userId) {
        List<CartInfo> cartInfoListDB = this.cartInfoMapper.selectCartListWithCurPrice(userId);
        for (CartInfo cartInfoCookie : cartListFromCookie) {
            boolean isMatch = false;
            for (CartInfo cartInfoDB : cartInfoListDB) {
                if(cartInfoCookie.getSkuId().equals(cartInfoDB.getSkuId())){
                    cartInfoDB.setSkuNum(cartInfoDB.getSkuNum()+cartInfoCookie.getSkuNum());
                    this.cartInfoMapper.updateByPrimaryKeySelective(cartInfoDB);
                    isMatch = true;
                }
            }
            if(!isMatch){
                cartInfoCookie.setUserId(userId);
                this.cartInfoMapper.insertSelective(cartInfoCookie);
            }
        }
        //会合数据，将数据库中的更新后的数据同步到redis缓存中
        List<CartInfo> cartInfoList = loadCartCache(userId);

        for (CartInfo cartInfoDB : cartInfoList) {
            for (CartInfo cartInfoCK : cartListFromCookie) {
                if(cartInfoDB.getSkuId().equals(cartInfoCK.getSkuId())){
                    if(cartInfoCK.getIsChecked().equals("1")){
                        cartInfoDB.setIsChecked("1");
                        checkCart(cartInfoDB.getSkuId(),userId,cartInfoDB.getIsChecked());
                    }
                }

            }
        }
        return cartInfoList;
    }

    @Override
    public void checkCart(String skuId, String userId, String isChecked) {
        Jedis jedis = this.jedisUtil.getJedis();
        String cartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        String cartJson = jedis.hget(cartKey, skuId);
        CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
        cartInfo.setIsChecked(isChecked);
        jedis.hset(cartKey, skuId, JSON.toJSONString(cartInfo));
        String checkedKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CHECKED_KEY_SUFFIX;
        if("1".equals(isChecked)){
            jedis.hset(checkedKey, skuId, JSON.toJSONString(cartInfo));
        }else{
            jedis.hdel(checkedKey, skuId);
        }
        jedis.close();

    }

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        Jedis jedis = this.jedisUtil.getJedis();
        String checkedKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CHECKED_KEY_SUFFIX;
        List<String> checkedStr = jedis.hvals(checkedKey);
        List<CartInfo> cartInfoList = new ArrayList<>();
        for (String checkedJson : checkedStr) {
            CartInfo cartInfo = JSON.parseObject(checkedJson, CartInfo.class);
            cartInfoList.add(cartInfo);
        }

        return cartInfoList;
    }
    @Override
    public List<CartInfo> loadCartCache(String userId) {
        List<CartInfo> cartInfoList = this.cartInfoMapper.selectCartListWithCurPrice(userId);
        if(cartInfoList==null&&cartInfoList.size()==0){
            return null;
        }else {
            Jedis jedis = this.jedisUtil.getJedis();
            String userKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
            Map<String, String> map = new HashMap<>();
            for (CartInfo cartInfo : cartInfoList) {
                map.put(cartInfo.getSkuId(),JSON.toJSONString(cartInfo));
            }
            jedis.hmset(userKey,map);
            jedis.close();
            return cartInfoList;
        }
    }
}
