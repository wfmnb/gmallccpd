package com.ccpd.gmall0822.service;

import com.ccpd.gmall0822.bean.OrderInfo;
import com.ccpd.gmall0822.enums.ProcessStatus;

import java.util.List;
import java.util.Map;

public interface OrderService {

    public String saveOrder(OrderInfo orderInfo);

    public String genTradeNo(String userId);

    public boolean verifyTradeNo(String userId,String tradeNo);

    public OrderInfo getOrderInfoByOrderId(String orderId);

    public void updateOrderStatus(String orderId, ProcessStatus processStatus);

    public void sendOrderStatus(String orderId);

    public List<Integer> checkExpiredCoupon();

    public void handleExpiredCoupon(Integer id);

    public List<Map> orderSplit(String orderId,String wareSkuMap);
}

