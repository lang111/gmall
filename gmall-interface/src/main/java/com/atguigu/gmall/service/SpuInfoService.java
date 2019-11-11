package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.BaseSaleAttr;
import com.atguigu.gmall.bean.SpuInfo;

import java.util.List;

public interface SpuInfoService  {

    List<SpuInfo> spuList(String baseCatalog3Id);

    List<SpuInfo> spuList(SpuInfo spuInfo);

    List<BaseSaleAttr> baseSaleAttrList();


}
