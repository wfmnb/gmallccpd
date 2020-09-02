package com.ccpd.gmall0822.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.ccpd.gmall0822.bean.UserInfo;
import com.ccpd.gmall0822.service.UserManageService;
import com.ccpd.gmall0822.user.mapper.UserMapper;
import com.ccpd.gmall0822.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

//需要使用dubbo的注解
@Service
public class UserManageServiceImpl implements UserManageService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    RedisUtil redisUtil;


    public String userKey_prefix = ":user";
    public String userinfoKey_suffix = ":info";
    public int userKey_timeOut = 60*60*24;

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

    @Override
    public UserInfo login(UserInfo userInfo) {
        //对密码进行MD5加密
        String password = DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());
        userInfo.setPasswd(password);
        UserInfo info = userMapper.selectOne(userInfo);

        if(info != null){
            //将用户写入Redis
            Jedis jedis = redisUtil.getJedis();
            String userKey = userKey_prefix + info.getId() + userinfoKey_suffix;
            jedis.setex(userKey,userKey_timeOut, JSON.toJSONString(info));
            jedis.close();
            return info;
        }
        return null;
    }

    @Override
    public UserInfo verify(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String userKey = userKey_prefix + userId + userinfoKey_suffix;
        String userJson = jedis.get(userKey);
        if(userJson != null){
            jedis.expire(userKey,userKey_timeOut);
            UserInfo userInfo = JSON.parseObject(userJson,UserInfo.class);
            return userInfo;
        }
        return null;
    }
}
