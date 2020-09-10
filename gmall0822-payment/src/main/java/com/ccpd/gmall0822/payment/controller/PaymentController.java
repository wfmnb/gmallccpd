package com.ccpd.gmall0822.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayAcquireRefundRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.ccpd.gmall0822.bean.OrderInfo;
import com.ccpd.gmall0822.bean.PaymentInfo;
import com.ccpd.gmall0822.enums.PaymentStatus;
import com.ccpd.gmall0822.payment.config.AlipayConfig;
import com.ccpd.gmall0822.service.OrderService;
import com.ccpd.gmall0822.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Controller
@CrossOrigin
public class PaymentController {

    @Reference
    OrderService orderService;

    @Autowired
    AlipayClient alipayClient;

    @Autowired
    PaymentService paymentService;

    @GetMapping("index")
    public String index(String orderId, Model model) {
        OrderInfo orderInfo = orderService.getOrderInfoByOrderId(orderId);
        model.addAttribute("orderId", orderInfo.getId());
        model.addAttribute("totalAmount", orderInfo.getTotalAmount());
        return "index";
    }

    @PostMapping("/alipay/submit")
    @ResponseBody
    public String submitPayment(String orderId, HttpServletResponse response) {
        //取得订单信息
        OrderInfo orderInfo = orderService.getOrderInfoByOrderId(orderId);

        //支付宝参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);
        long currentTimeMillis = System.currentTimeMillis();
        String outTradeNo = "CCPD-" + orderId + "-" + currentTimeMillis;
        String productNo = "FAST_INSTANT_TRADE_PAY";
        BigDecimal totalAmount = orderInfo.getTotalAmount();
        String tradeBody = orderInfo.getTradeBody();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("out_trade_no", outTradeNo);
        jsonObject.put("product_code", productNo);
        jsonObject.put("total_amount", totalAmount);
        jsonObject.put("subject", tradeBody);
        alipayRequest.setBizContent(jsonObject.toJSONString());
        String submitHtml = "";
        try {
            submitHtml = alipayClient.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }


        //保存支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        paymentInfo.setOutTradeNo(outTradeNo);
        paymentInfo.setTotalAmount(totalAmount);
        paymentInfo.setSubject(tradeBody);
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        paymentService.savPaymentInfo(paymentInfo);
        paymentService.sendDelayPaymentResult(outTradeNo,30,3);

        response.setContentType("text/html;charset=UTF-8");
        return submitHtml;
    }

    @PostMapping("alipay/callback/notify")
    public String notify(@RequestParam Map<String, String> paraMap, HttpServletRequest request) throws AlipayApiException {
        //验签
        String sign = paraMap.get("sign");
        boolean ifPass = AlipaySignature.rsaCheckV1(paraMap, AlipayConfig.alipay_public_key, "utf-8", AlipayConfig.sign_type);
        if (ifPass) {
            String tradeStatus = paraMap.get("trade_status");
            String totalAmount = paraMap.get("total_amount");
            String outTradeNo = paraMap.get("out_trade_no");
            //支付宝付款成功
            if ("TRADE_SUCCESS".equals(tradeStatus)) {
                PaymentInfo paymentInfoQuery = new PaymentInfo();
                paymentInfoQuery.setOutTradeNo(outTradeNo);
                PaymentInfo paymentInfo = paymentService.getPaymentInfo(paymentInfoQuery);
                if (paymentInfo.getTotalAmount().compareTo(new BigDecimal(totalAmount)) == 0) {
                    if (PaymentStatus.UNPAID.equals(paymentInfo.getPaymentStatus())) {
                        PaymentInfo paymentInfoUpdate = new PaymentInfo();
                        paymentInfoUpdate.setPaymentStatus(PaymentStatus.PAID);
                        paymentInfoUpdate.setAlipayTradeNo(paraMap.get("trade_no"));
                        paymentInfoUpdate.setCallbackTime(new Date());
                        paymentInfoUpdate.setCallbackContent(JSON.toJSONString(paraMap));

                        paymentService.updatePaymentInfoByOutTradeNo(outTradeNo, paymentInfoUpdate);
                        paymentService.sendPaymentResult(paymentInfo.getOrderId(),"success");
                        return "success";
                    } else if (PaymentStatus.ClOSED.equals(paymentInfo.getPaymentStatus())) {
                        return "fail";
                    } else if (PaymentStatus.PAID.equals(paymentInfo.getPaymentStatus())) {
                        return "success";
                    }
                }
            }
        }
        return "fail";
    }

    @GetMapping("alipay/callback/return")
    @ResponseBody
    public String alipayReturn() {
        return "交易成功！";
    }

    @GetMapping("refund")
    @ResponseBody
    public String refund(@RequestParam String orderId) throws AlipayApiException {
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        PaymentInfo paymentInfoQuery = new PaymentInfo();
        paymentInfoQuery.setOrderId(orderId);
        PaymentInfo paymentInfo = paymentService.getPaymentInfo(paymentInfoQuery);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("out_trade_no", paymentInfo.getOutTradeNo());
        jsonObject.put("refund_amount", paymentInfo.getTotalAmount());
        request.setBizContent(jsonObject.toJSONString());
        AlipayTradeRefundResponse response = alipayClient.execute(request);

        if (response.isSuccess()) {
            PaymentInfo paymentInfoUpdate = new PaymentInfo();
            paymentInfoUpdate.setPaymentStatus(PaymentStatus.PAY_REFUND);
            paymentService.updatePaymentInfoByOutTradeNo(paymentInfo.getOutTradeNo(), paymentInfoUpdate);
            return "success";
        }
        return response.getSubCode() + ":" + response.getSubMsg();
    }

    @GetMapping("sendPaymentResult")
    @ResponseBody
    public String sendPaymentResult(String orderId){
        paymentService.sendPaymentResult(orderId,"success");
        return "success";
    }
}
