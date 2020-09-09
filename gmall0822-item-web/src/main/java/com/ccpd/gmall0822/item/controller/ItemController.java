package com.ccpd.gmall0822.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.ccpd.gmall0822.bean.SkuInfo;
import com.ccpd.gmall0822.bean.SpuSaleAttr;
import com.ccpd.gmall0822.config.LoginRequire;
import com.ccpd.gmall0822.service.ListService;
import com.ccpd.gmall0822.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {


    @Reference
    ManageService manageService;

    @Reference
    ListService listService;

    @GetMapping("{skuId}.html")
    public String getSkuInfo(@PathVariable("skuId") String skuId,HttpServletRequest request){
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        List<SpuSaleAttr> saleAttrList  = manageService.getSpuSaleAttrListBySpuIdChecked(skuId, skuInfo.getSpuId());
        Map skuValueIdsMap = manageService.getSkuValueIdsMap(skuInfo.getSpuId());
        String skuValueIdsJson = JSON.toJSONString(skuValueIdsMap);
        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("saleAttrList",saleAttrList);
        request.setAttribute("skuValueIdsJson",skuValueIdsJson);
        //listService.incrHotScore(skuId);
        return "item";
    }
}


