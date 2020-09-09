package com.ccpd.gmall0822.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.ccpd.gmall0822.bean.*;
import com.ccpd.gmall0822.config.LoginRequire;
import com.ccpd.gmall0822.enums.OrderStatus;
import com.ccpd.gmall0822.enums.ProcessStatus;
import com.ccpd.gmall0822.service.CartService;
import com.ccpd.gmall0822.service.ManageService;
import com.ccpd.gmall0822.service.OrderService;
import com.ccpd.gmall0822.service.UserManageService;
import com.ccpd.gmall0822.util.HttpClientUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import sun.net.www.http.HttpClient;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@Controller
public class OrderController {

    //需要使用dubbo的注解
    @Reference
    UserManageService userManageService;

    @Reference
    CartService cartService;

    @Reference
    ManageService manageService;

    @Reference
    OrderService orderService;

    @GetMapping("trade")
    @LoginRequire
    public String trade(HttpServletRequest request) {
        BigDecimal totalPrice = new BigDecimal("0");
        String userId = (String) request.getAttribute("userId");
        List<UserAddress> userAddressList = userManageService.getUserAddress(userId);
        List<CartInfo> checkedCartList = cartService.getCheckedCartList(userId);
        if (checkedCartList != null) {
            for (CartInfo cartInfo : checkedCartList) {
                totalPrice = totalPrice.add(cartInfo.getSkuPrice().multiply(new BigDecimal(cartInfo.getSkuNum())));
            }
        }
        String tradeNo = orderService.genTradeNo(userId);
        request.setAttribute("tradeNo",tradeNo);
        request.setAttribute("totalPrice", totalPrice);
        request.setAttribute("checkedCartList", checkedCartList);
        request.setAttribute("userAddressList", userAddressList);

        return "trade";
    }

    @PostMapping("submitOrder")
    @LoginRequire
    public String submitOrder(OrderInfo orderInfo, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        String tradeNo = request.getParameter("tradeNo");
        boolean bool = orderService.verifyTradeNo(userId, tradeNo);
        if(!bool){
            request.setAttribute("errMsg","页面已失效，请重新结算！");
            return "tradeFail";
        }
        orderInfo.setCreateTime(new Date());
        orderInfo.setExpireTime(DateUtils.addMinutes((new Date()), 15));
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        orderInfo.sumTotalAmount();
        orderInfo.setUserId(userId);
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            SkuInfo skuInfo = manageService.getSkuInfo(orderDetail.getSkuId());
            orderDetail.setSkuName(skuInfo.getSkuName());
            orderDetail.setImgUrl(skuInfo.getSkuDefaultImg());
            if(!orderDetail.getOrderPrice().equals(skuInfo.getPrice())){
                request.setAttribute("errMsg","商品价格已发送变动请重新下单！");
                return "tradeFail";
            }
        }
        List<String> erroList = Collections.synchronizedList(new ArrayList<>());
        Stream<CompletableFuture<String>> completableFutureStream = orderDetailList.stream().map(orderDetail ->
                CompletableFuture.supplyAsync(() -> checkSkuNum(orderDetail)).whenComplete((hasStock, ex) -> {
                    if ("0".equals(hasStock)) {
                        erroList.add(orderDetail.getSkuName());
                    }
                })
        );
        CompletableFuture[] completableFutures = completableFutureStream.toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(completableFutures).join();

        if(erroList != null && erroList.size() > 0){
            StringBuilder stringBuilder = new StringBuilder();
            for (String skuName : erroList) {
                stringBuilder.append("商品："+skuName+"库存暂时不足!");
            }
            request.setAttribute("errMsg",stringBuilder.toString());
            return "tradeFail";
        }

        String orderId = orderService.saveOrder(orderInfo);
        return "redirect://payment.gmall.com/index?orderId="+orderId;
    }

    public String checkSkuNum(OrderDetail orderDetail){
        String hasStock = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + orderDetail.getSkuId() + "&num=" + orderDetail.getSkuNum());
        return hasStock;
    }
}
