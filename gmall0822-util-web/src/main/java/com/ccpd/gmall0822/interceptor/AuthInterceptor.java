package com.ccpd.gmall0822.interceptor;

import com.alibaba.fastjson.JSON;
import com.ccpd.gmall0822.config.LoginRequire;
import com.ccpd.gmall0822.constans.WebConst;
import com.ccpd.gmall0822.util.CookieUtil;
import com.ccpd.gmall0822.util.HttpClientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static com.ccpd.gmall0822.constans.WebConst.COOKIE_MAXAGE;
import static com.ccpd.gmall0822.constans.WebConst.VERIFY_ADDRESS;

//增加拦截器
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取URL中的newToken
        String token = request.getParameter("newToken");
        Map userMap = new HashMap();
        if(token != null){
            //token不为空，将token写入浏览器
            CookieUtil.setCookie(request,response,"token",token, COOKIE_MAXAGE,false);
        }else{
            //获取浏览器的中的token
            token = CookieUtil.getCookieValue(request,"token",false);
        }

        if(token != null){
            //token不为空，获取token中的用户信息
            userMap = getUserMapFromToken(token);
            String nickName = (String) userMap.get("nickName");
            request.setAttribute("nickName",nickName);
            //获取LoginRequire注解
            HandlerMethod handlerMethod = (HandlerMethod)handler;
            LoginRequire loginRequire = handlerMethod.getMethodAnnotation(LoginRequire.class);
            if(loginRequire != null){
                //注解不为空则，证明需要验证用户是否登录
                //获取用户访问IP，获得该IP，需要在nginx中进行配置proxy_set_header X-forwarded-for $proxy_add_x_forwarded_for;
                String currentIp = request.getHeader("X-forwarded-for");
                //向认证中心发送请求，验证用户是否登录
                String result = HttpClientUtil.doGet(VERIFY_ADDRESS + "?token=" + token + "&currentIp=" + currentIp);
                if("success".equals(result)){
                    return true;
                //不需要验证
                }else if(!loginRequire.autoRedirect()){
                    return true;
                //验证失败强制跳转登录界面
                }else{
                    autoRedirect(request,response);
                    return false;
                }
            }
        }else{
            //token为空强制跳转登录界面
            autoRedirect(request,response);
            return false;
        }

        return true;
    }

    public Map getUserMapFromToken(String token){
        //获取token中的用户信息部分
        String userBase64 = StringUtils.substringBetween(token,".");
        //获取一个Base64UrlCodec对象
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        //将用户信息解析为bytes数组
        byte[] userBytes = base64UrlCodec.decode(userBase64);
        //将bytes数组转换为JSON串
        String userJson = new String(userBytes);
        Map userMap = JSON.parseObject(userJson, Map.class);
        return userMap;
    }

    public void autoRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //获取requestURL
        String requestUrl = request.getRequestURL().toString();
        //将requestURL中的特殊字符转译
        String encodeUrl = URLEncoder.encode(requestUrl, "UTF-8");
        //重定向并设置originUrl
        response.sendRedirect(WebConst.LOGIN_ADDRESS+"?originUrl="+encodeUrl);
    }
}
