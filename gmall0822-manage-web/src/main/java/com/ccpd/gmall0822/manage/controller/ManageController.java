package com.ccpd.gmall0822.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.ccpd.gmall0822.bean.*;
import com.ccpd.gmall0822.service.ManageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin//解决跨域问题
public class ManageController {

    @Reference
    ManageService manageService;

    @PostMapping("getCatalog1")
    public List<BaseCatalog1> getBaseCatalog1(){
        return manageService.getCatalog1();
    }

    @PostMapping("getCatalog2")
    public List<BaseCatalog2> getBaseCatalog2(String catalog1Id){
        return manageService.getCatalog2(catalog1Id);
    }

    @PostMapping("getCatalog3")
    public List<BaseCatalog3> getBaseCatalog3(String catalog2Id){
        return manageService.getCatalog3(catalog2Id);
    }

    @GetMapping("attrInfoList")
    public List<BaseAttrInfo> attrInfoList(String catalog3Id){
        return manageService.getAttrList(catalog3Id);
    }

    @PostMapping("saveAttrInfo")
    public String saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        manageService.saveAttrInfo(baseAttrInfo);
        return "success";
    }

    @PostMapping("getAttrValueList")
    public List<BaseAttrValue> getAttrValueList(String attrId){

        return manageService.getAttrInfo(attrId).getAttrValueList();
    }
}
