package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;

import com.atguigu.gmall.bean.BaseSaleAttr;
import com.atguigu.gmall.bean.SpuInfo;
import com.atguigu.gmall.service.ManageService;
import com.atguigu.gmall.service.SpuInfoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class SpuManageController {
    @Reference
    private SpuInfoService spuInfoService;
    @Reference
    private ManageService manageService;

    //http://localhost:8082/spuList?catalog3Id=61
   /* @ResponseBody
    @RequestMapping("spuList")
    public List<SpuInfo> spuList(String baseCatalog3Id){
       return this.spuInfoService.spuList(baseCatalog3Id);

    }*/

    @ResponseBody
    @RequestMapping("spuList")
    public List<SpuInfo> spuList(SpuInfo spuInfo){
        return this.spuInfoService.spuList(spuInfo);

    }
    //http://localhost:8082/baseSaleAttrList
    @ResponseBody
    @RequestMapping("baseSaleAttrList")
    public List<BaseSaleAttr> baseSaleAttrList(){
        return this.spuInfoService.baseSaleAttrList();
    }

    //http://localhost:8082/saveSpuInfo

    @RequestMapping("saveSpuInfo")
    public void saveSpuInfo(@RequestBody SpuInfo spuInfo){
        this.manageService.saveSpuInfo(spuInfo);
    }


}
