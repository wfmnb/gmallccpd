package com.ccpd.gmall0822.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.ccpd.gmall0822.bean.CartInfo;
import com.ccpd.gmall0822.config.LoginRequire;
import com.ccpd.gmall0822.constans.WebConst;
import com.ccpd.gmall0822.service.CartService;
import com.ccpd.gmall0822.util.CookieUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

@Controller
public class CartController {

    @Reference
    CartService cartService;

    public static final String TOKEN_NAME = "user_tmp_cart";

    @PostMapping("addToCart")
    @LoginRequire(autoRedirect = false)
    public String addToCart(@RequestParam("skuId")String skuId, @RequestParam("num")Integer num, HttpServletRequest request, HttpServletResponse response){
        //获取userId判断用户登录状态
        String userId = (String)request.getAttribute("userId");
        if(userId == null){
            //获取cookie中的userId
            userId = CookieUtil.getCookieValue(request, "user_tmp_cart", false);
            if(userId == null){
                //cookie中没有则生成
                userId = UUID.randomUUID().toString();
                CookieUtil.setCookie(request,response,"user_tmp_cart",userId, WebConst.COOKIE_MAXAGE,false);
            }
        }
        CartInfo cartInfo = cartService.addCart(userId, skuId, num);
        cartInfo.setSkuNum(num);
        request.setAttribute("cartInfo",cartInfo);
        return "success";
    }

    @GetMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public String cartList(HttpServletRequest request,HttpServletResponse response){
        String userId = (String)request.getAttribute("userId");
        String userIdTmp = CookieUtil.getCookieValue(request,"user_tmp_cart",false);
        List<CartInfo> cartInfos = null;
        List<CartInfo> tmpCartInfos = null;
        //用戶登陸且token不为空，合并购物车
        /*if(userId != null & userIdTmp != null){
            List<CartInfo> cartInfos = cartService.mergeCart(userIdTmp, userId);
            request.setAttribute("cartInfos",cartInfos);
            //CookieUtil.deleteCookie(request,response,"user_tmp_cart");
        }
        else if(userId != null){
            List<CartInfo> cartInfos = cartService.cartList(userId);
            if(cartInfos != null && cartInfos.size() > 0){
                request.setAttribute("cartInfos",cartInfos);
            }
        }else if(userIdTmp != null){
            List<CartInfo> cartInfos = cartService.cartList(userIdTmp);
            if(cartInfos != null && cartInfos.size() > 0){
                request.setAttribute("cartInfos",cartInfos);
            }
        }*/
        if(userId != null){
            cartInfos = cartService.cartList(userId);
            if(userIdTmp != null){
                tmpCartInfos = cartService.cartList(userIdTmp);
            }
            if(tmpCartInfos != null){
                cartInfos = cartService.mergeCart(userIdTmp, userId);
            }
        }else{
            if(userIdTmp != null){
                tmpCartInfos = cartService.cartList(userIdTmp);
                cartInfos = tmpCartInfos;
            }
        }
        request.setAttribute("cartInfos",cartInfos);
        return "cartList";
    }

    @PostMapping("checkCart")
    @LoginRequire(autoRedirect = false)
    @ResponseBody
    public void checkCart(@RequestParam("isChecked") String isChecked,@RequestParam("skuId") String skuId,HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        if(userId == null){
             userId = CookieUtil.getCookieValue(request, "user_tmp_cart", false);
        }
        cartService.checkCart(isChecked,userId,skuId);
    }
}
