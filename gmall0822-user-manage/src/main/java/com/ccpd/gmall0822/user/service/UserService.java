package com.ccpd.gmall0822.user.service;

import com.ccpd.gmall0822.user.bean.UserInfo;

import java.util.List;

public interface UserService {

    List<UserInfo> getUserInfoListAll();

    void addUser(UserInfo userInfo);

    void updateUser(UserInfo userInfo);

    void updateUserByName(String name,UserInfo userInfo);

    void delUser(UserInfo userInfo);

    UserInfo getUserInfo(UserInfo userInfo);
}

