package com.ccpd.gmall0822.service;

import com.ccpd.gmall0822.bean.OrderInfo;

public interface OrderService {

    public String saveOrder(OrderInfo orderInfo);

    public String genTradeNo(String userId);

    public boolean verifyTradeNo(String userId,String tradeNo);

    public OrderInfo getOrderInfoByOrderId(String orderId);
}

