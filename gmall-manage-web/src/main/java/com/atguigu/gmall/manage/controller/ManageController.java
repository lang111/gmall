package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;

import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@CrossOrigin
@ComponentScan(basePackages = "com.atguigu.gmall")
public class ManageController  {
    @Reference
    private ListService listService;

   @Reference
    private ManageService manageService;

    @RequestMapping("index")
    public String index(){
        return "index";
    }

    @RequestMapping("getCatalog1")
    @ResponseBody
    public List<BaseCatalog1> getCatalog1(){
        List<BaseCatalog1> catalog1 = this.manageService.getCatalog1();
        return catalog1;
    }

    //http://localhost:8082/getCatalog2?catalog1Id=4
    @RequestMapping("getCatalog2")
    @ResponseBody
    public List<BaseCatalog2> getCatalog2(String catalog1Id){
        List<BaseCatalog2> catalog2 = this.manageService.getCatalog2(catalog1Id);
        System.out.println(catalog2);
        return catalog2;
    }

    @RequestMapping("getCatalog3")
    @ResponseBody
    public List<BaseCatalog3> getCatalog3(String catalog2Id){
        List<BaseCatalog3> catalog3 = this.manageService.getCatalog3(catalog2Id);
        System.out.println(catalog3);
        return catalog3;
    }

    //attrInfoList?catalog3Id=2
/*    @RequestMapping("attrInfoList")
    @ResponseBody
    public List<BaseAttrInfo> attrInfoList(String BaseCatalog3Id){
        List<BaseAttrInfo> BaseAttrInfoList = this.manageService.getAttrList(BaseCatalog3Id);
        return BaseAttrInfoList;
    }*/

    //http://localhost:8082/saveAttrInfo?
    @RequestMapping("saveAttrInfo")
    @ResponseBody
    public void saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        this.manageService.saveAttrInfo(baseAttrInfo);

    }

    //http://localhost:8082/getAttrValueList?attrId=23

    @ResponseBody
    @PostMapping(value = "getAttrValueList")
    public List<BaseAttrValue> getAttrValueList(String attrId){

        BaseAttrInfo baseAttrInfo = this.manageService.getAttrInfo(attrId);


        return baseAttrInfo.getAttrValueList();
    }

    @RequestMapping("onSale")
    @ResponseBody
    public String onSale(String skuId){
        SkuInfo skuInfo = this.manageService.getSkuInfo(skuId);
        SkuLsInfo skuLsInfo = new SkuLsInfo();
        BeanUtils.copyProperties(skuInfo, skuLsInfo);
        this.listService.saveSkuInfo(skuLsInfo);
        return "ok";
    }


}
