package com.atguigu.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {
    @Reference
    private ListService listService;
    @Reference
    private ManageService manageService;


    @RequestMapping("list.html")
    public String getList(SkuLsParams skuLsParams, HttpServletRequest request){
        skuLsParams.setPageSize(2);
        SkuLsResult skuLsResult = this.listService.search(skuLsParams);

        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();
        request.setAttribute("skuLsInfoList",skuLsInfoList);
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        System.out.println("attrValueIdList==================>"+attrValueIdList);
        List<BaseAttrInfo> baseAttrInfoList = this.manageService.getAttrList(attrValueIdList);

        ArrayList<BaseAttrValue> baseAttrValueList = new ArrayList<>();

        String urlParam = makeUrlParam(skuLsParams);

        for (Iterator<BaseAttrInfo> iterator = baseAttrInfoList.iterator(); iterator.hasNext(); ) {
            BaseAttrInfo attrInfo = iterator.next();
            List<BaseAttrValue> attrValueList = attrInfo.getAttrValueList();
            for (BaseAttrValue baseAttrValue : attrValueList) {
               if(skuLsParams.getValueId()!=null&&skuLsParams.getValueId().length>0){
                   for (String skuValueId : skuLsParams.getValueId()) {
                       if(baseAttrValue.getId().equals(skuValueId)){
                           iterator.remove();
                           BaseAttrValue baseAttrValuenew = new BaseAttrValue();
                           String valueName = attrInfo.getAttrName() +":"+ baseAttrValue.getValueName();
                           baseAttrValuenew.setValueName(valueName);

                           String urlParamNew = makeUrlParam(skuLsParams,skuValueId);
                           baseAttrValuenew.setUrlParam(urlParamNew);
                           baseAttrValueList.add(baseAttrValuenew);

                       }
                   }
               }
            }

        }
        request.setAttribute("totalPages", skuLsResult.getTotalPages());
        request.setAttribute("pageNo", skuLsParams.getPageNo());
        request.setAttribute("baseAttrValueList",baseAttrValueList);
        request.setAttribute("baseAttrInfoList",baseAttrInfoList);
        request.setAttribute("urlParam",urlParam);
        request.setAttribute("keyword", skuLsParams.getKeyword());

        return "list";
    }

    private String makeUrlParam(SkuLsParams skuLsParams,String ...excludeValueIds) {
        String urlParam = "";
        /*http://list.gmall.com/list.html?catalog3Id=61&valueId=13 */
        if(skuLsParams.getCatalog3Id()!=null){
            urlParam+="catalog3Id="+skuLsParams.getCatalog3Id();
        }

        if(skuLsParams.getKeyword()!=null){
            urlParam+="keyword="+skuLsParams.getKeyword();
        }



        if(skuLsParams.getValueId()!=null){
            for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                String valueId = skuLsParams.getValueId()[i];
                if(excludeValueIds!=null&&excludeValueIds.length>0){
                    String excludeValueId = excludeValueIds[0];
                    if(valueId.equals(excludeValueId)){
                        continue;
                    }
                }

                if(urlParam.length()>0){
                    urlParam+="&";
                }
                urlParam+="valueId="+skuLsParams.getValueId()[i];
            }
        }

        return urlParam;
    }

}
