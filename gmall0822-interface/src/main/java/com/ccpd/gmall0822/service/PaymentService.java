package com.ccpd.gmall0822.service;

import com.ccpd.gmall0822.bean.PaymentInfo;

public interface PaymentService {
    public void savPaymentInfo(PaymentInfo paymentInfo);

    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfo);

    public void updatePaymentInfoByOutTradeNo(String outTradeNo,PaymentInfo paymentInfo);

    public void sendPaymentResult(String orderId,String result);

    public void sendDelayPaymentResult(String outTradeNo,int delaySec,int checkCount);
}
