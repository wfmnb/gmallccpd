package com.ccpd.gmall0822.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.ccpd.gmall0822.bean.BaseAttrInfo;
import com.ccpd.gmall0822.bean.BaseAttrValue;
import com.ccpd.gmall0822.bean.SkuLsParams;
import com.ccpd.gmall0822.bean.SkuLsResult;
import com.ccpd.gmall0822.service.ListService;
import com.ccpd.gmall0822.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {


    @Reference
    ListService listService;

    @Reference
    ManageService manageService;

    @GetMapping("list.html")
    public String getList(SkuLsParams skuLsParams, Model model){
        SkuLsResult skuLsResult = listService.search(skuLsParams);
        List<BaseAttrInfo> attrList = manageService.getAttrList(skuLsResult.getAttrValueIdList());
        List<BaseAttrValue> baseAttrValues = new ArrayList<>();
        for (Iterator<BaseAttrInfo> iterator = attrList.iterator(); iterator.hasNext(); ) {
            BaseAttrInfo next = iterator.next();
            if(skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0){
                for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                    for (BaseAttrValue baseAttrValue : next.getAttrValueList()) {
                        if(baseAttrValue.getId().equals(skuLsParams.getValueId()[i])){
                            String makeUrl = makeUrl(skuLsParams, baseAttrValue.getId());
                            baseAttrValue.setUrlParam(makeUrl);
                            baseAttrValues.add(baseAttrValue);
                            iterator.remove();
                        }
                    }
                }
            }
        }

        String oldUrl = makeUrl(skuLsParams);
        JSON.toJSONString(skuLsResult);
        model.addAttribute("skuLsResult",skuLsResult);
        model.addAttribute("attrList",attrList);
        model.addAttribute("oldUrl",oldUrl);
        model.addAttribute("keyword",skuLsParams.getKeyword());
        model.addAttribute("baseAttrValues",baseAttrValues);
        model.addAttribute("totalPages", skuLsResult.getTotalPages());
        model.addAttribute("pageNo",skuLsParams.getPageNo());
        return "list";
    }

    public String makeUrl(SkuLsParams skuLsParams,String... exValueId){
        StringBuilder stringBuilder = new StringBuilder();
        if(skuLsParams != null){
            if(skuLsParams.getKeyword() != null){
                stringBuilder.append("keyword="+skuLsParams.getKeyword());
            }else{
                stringBuilder.append("catalog3Id="+skuLsParams.getCatalog3Id());
            }

            if(skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0){
                for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                    if(exValueId != null && exValueId.length > 0){
                        if(exValueId[0].equals(skuLsParams.getValueId()[i])){
                            continue;
                        }
                    }
                    if(stringBuilder.length() > 0){
                        stringBuilder.append("&valueId="+skuLsParams.getValueId()[i]);
                    }else{
                        stringBuilder.append("valueId="+skuLsParams.getValueId()[i]);
                    }
                }
            }
        }
        return stringBuilder.toString();
    }
}

