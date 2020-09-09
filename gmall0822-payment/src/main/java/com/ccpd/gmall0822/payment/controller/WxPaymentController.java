package com.ccpd.gmall0822.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.ccpd.gmall0822.bean.OrderInfo;
import com.ccpd.gmall0822.payment.util.HttpClient;
import com.ccpd.gmall0822.payment.util.StreamUtil;
import com.ccpd.gmall0822.service.OrderService;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
public class WxPaymentController {

    @Value("${appid}")
    String appid;

    @Value("${partner}")
    String partner;

    @Value("${partnerkey}")
    String partnerkey;

    @Reference
    OrderService orderService;

    @PostMapping("/wx/submit")
    public Map wxSubmit(String orderId) throws Exception {
        OrderInfo orderInfo = orderService.getOrderInfoByOrderId(orderId);

        Map paraMap = new HashMap();
        paraMap.put("appid",appid);
        paraMap.put("mch_id",partner);
        paraMap.put("nonce_str", WXPayUtil.generateNonceStr());
        paraMap.put("body", orderInfo.getTradeBody());
        long currentTimeMillis = System.currentTimeMillis();
        String outTradeNo = "CCPD-" + orderId + "-" + currentTimeMillis;
        paraMap.put("out_trade_no", outTradeNo);
        paraMap.put("total_fee", orderInfo.getTotalAmount().multiply(new BigDecimal(100)).toBigInteger().toString());
        paraMap.put("spbill_create_ip", "127.0.0.1");
        paraMap.put("notify_url", "http://payment.nat123.fun/wx/callback/notify");
        paraMap.put("trade_type", "NATIVE");

        String xmlPara = WXPayUtil.generateSignedXml(paraMap,partnerkey);
        HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
        httpClient.setXmlParam(xmlPara);
        httpClient.post();
        String content = httpClient.getContent();
        Map<String, String> resultMap = WXPayUtil.xmlToMap(content);
        if(resultMap.get("code_url") !=null ){
            return resultMap;
        }
        return null;
    }

    @PostMapping("/wx/callback/notify")
    public String notify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ServletInputStream inputStream = request.getInputStream();
        String xmlString = StreamUtil.inputStream2String(inputStream, "utf-8");

        if(WXPayUtil.isSignatureValid(xmlString,partnerkey)){
            Map<String, String> paraMap = WXPayUtil.xmlToMap(xmlString);
            String result_code = paraMap.get("result_code");
            if("SUCCESS".equals(result_code)){
                Map returnMap = new HashMap();
                returnMap.put("return_code",result_code);
                returnMap.put("return_msg","OK");

                String returnXml = WXPayUtil.mapToXml(returnMap);
                response.setContentType("text/xml");

                return returnXml;
            }
            else {
                System.out.println(paraMap.get("return_code")+"-------"+paraMap.get("return_msg"));
            }
        }
        return null;
    }
}
