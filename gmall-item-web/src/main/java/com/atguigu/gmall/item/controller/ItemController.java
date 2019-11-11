package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

@Controller
public class ItemController {

    @Reference
    private ManageService manageService;
    @Reference
    private ListService listService;

    @RequestMapping("/{skuId}.html")
    //@LoginRequire
    public String index(@PathVariable String skuId, HttpServletRequest request){
        SkuInfo skuInfo = this.manageService.getSkuInfo(skuId);
        request.setAttribute("skuInfo",skuInfo);
        System.out.println(skuId);
        List<SpuSaleAttr> spuSaleAttrList= this.manageService.getSpuSaleAttrListCheckBySku(skuInfo);
        request.setAttribute("spuSaleAttrList",spuSaleAttrList);
        List<SkuSaleAttrValue> skuSaleAttrValueList = this.manageService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());
        String key = "";
        HashMap<String, String> keyMap = new HashMap<>();
        for (int i = 0; i < skuSaleAttrValueList.size(); i++) {
            SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueList.get(i);

            if(key.length()>0){
                key+="|";
            }
            key+=skuSaleAttrValue.getSaleAttrValueId();

            if((i+1)==skuSaleAttrValueList.size()||!skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueList.get(i+1).getSkuId())){
                keyMap.put(key,skuSaleAttrValue.getSkuId());
                key="";
            }


        }
        String valuesSkuJson  = JSON.toJSONString(keyMap);
        request.setAttribute("valuesSkuJson",valuesSkuJson);
        this.listService.incrHotScore(skuId);

        return "item";

    }

}
