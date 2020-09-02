package com.ccpd.gmall0822.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.ccpd.gmall0822.bean.UserInfo;
import com.ccpd.gmall0822.util.JwtUtil;
import com.ccpd.gmall0822.service.UserManageService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController
{

    @Reference
    UserManageService userManageService;

    @Value("${token.key}")
    String signKey;

    @RequestMapping("index")
    public String index(HttpServletRequest request){
        String originUrl = request.getParameter("originUrl");
        request.setAttribute("originUrl",originUrl);
        return "index";
    }


    //登录
    @PostMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo, HttpServletRequest request){
        String ipAddr = request.getHeader("X-forwarded-for");
        if(userInfo != null){
            UserInfo info = userManageService.login(userInfo);
            if(info != null){
                Map<String, Object> map = new HashMap<>();
                map.put("userId",info.getId());
                map.put("nickName",info.getNickName());
                //使用JWT工具生成token，并响应给浏览器
                String token = JwtUtil.encode(signKey, map, ipAddr);
                return token;
            }else{
                return "fail";
            }
        }
        return "index";
    }

    //认证
    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request){
        String token = request.getParameter("token");
        String currentIp = request.getParameter("currentIp");

        Map<String, Object> userMap = JwtUtil.decode(token, signKey, currentIp);
        if(userMap != null){
            UserInfo userInfo = userManageService.verify(userMap.get("userId").toString());
            if(userInfo != null){
                return "success";
            }
        }
        return "fail";
    }
}
