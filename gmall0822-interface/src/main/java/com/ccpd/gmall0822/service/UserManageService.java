package com.ccpd.gmall0822.service;

import com.ccpd.gmall0822.bean.UserInfo;

import java.util.List;

public interface UserManageService {

    public List<UserInfo> getUserInfoListAll();

    public void addUser(UserInfo userInfo);

    public void updateUser(UserInfo userInfo);

    public void updateUserByName(String name,UserInfo userInfo);

    public void delUser(UserInfo userInfo);

    public UserInfo getUserInfo(UserInfo userInfo);

    public UserInfo login(UserInfo userInfo);

    public UserInfo verify(String userId);

}

