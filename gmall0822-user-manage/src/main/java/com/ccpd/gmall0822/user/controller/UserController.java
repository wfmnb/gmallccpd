package com.ccpd.gmall0822.user.controller;


import com.ccpd.gmall0822.bean.UserInfo;
import com.ccpd.gmall0822.service.UserManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    UserManageService userManageService;

    @GetMapping("allusers")
    public List<UserInfo> getAllUsers(){
        return userManageService.getUserInfoListAll();
    }

    @PostMapping("addUser")
    public String addUser(UserInfo userInfo){
        userManageService.addUser(userInfo);
        return "success";
    }

    @PostMapping("updateUser")
    public String updateUser(UserInfo userInfo){
        userManageService.updateUser(userInfo);
        return "success";
    }

    @PostMapping("updateUserByName")
    public String updateUserByName(UserInfo userInfo){
        userManageService.updateUserByName(userInfo.getName(),userInfo);
        return "success";
    }

    @PostMapping("delUser")
    public String deleteUser(UserInfo userInfo){
        userManageService.delUser(userInfo);
        return "success";
    }

    @GetMapping("getUser")
    public UserInfo getUser(UserInfo userInfo){
        return userManageService.getUserInfo(userInfo);
    }
}
