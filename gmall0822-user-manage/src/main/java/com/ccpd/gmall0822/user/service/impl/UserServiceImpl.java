package com.ccpd.gmall0822.user.service.impl;

import com.ccpd.gmall0822.user.bean.UserInfo;
import com.ccpd.gmall0822.user.mapper.UserMapper;
import com.ccpd.gmall0822.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    UserMapper userMapper;

    @Override
    public List<UserInfo> getUserInfoListAll() {
        //查询所有用户
        List<UserInfo> userInfos = userMapper.selectAll();

        return userInfos;
    }

    @Override
    public void addUser(UserInfo userInfo) {
        //有选择的插入
        userMapper.insertSelective(userInfo);
    }

    @Override
    public void updateUser(UserInfo userInfo) {
        userMapper.updateByPrimaryKeySelective(userInfo);
    }

    @Override
    public void updateUserByName(String name, UserInfo userInfo) {
        Example example = new Example(UserInfo.class);
        example.createCriteria().andEqualTo("name",name);
        userMapper.updateByExampleSelective(userInfo,example);
    }

    @Override
    public void delUser(UserInfo userInfo) {
        userMapper.delete(userInfo);
    }

    @Override
    public UserInfo getUserInfo(UserInfo userInfo) {
        return userMapper.selectOne(userInfo);
    }
}
