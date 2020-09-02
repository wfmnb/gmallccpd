package com.ccpd.gmall0822.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.ccpd.gmall0822.bean.UserInfo;
import com.ccpd.gmall0822.service.UserManageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    //需要使用dubbo的注解
    @Reference
    UserManageService userManageService;

    @GetMapping("trade")
    public UserInfo trade(UserInfo userInfo){
        return userManageService.getUserInfo(userInfo);
    }
}
