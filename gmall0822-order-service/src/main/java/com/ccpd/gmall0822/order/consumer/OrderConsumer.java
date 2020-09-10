package com.ccpd.gmall0822.order.consumer;

import com.alibaba.dubbo.config.annotation.Reference;
import com.ccpd.gmall0822.enums.ProcessStatus;
import com.ccpd.gmall0822.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class OrderConsumer {

    @Autowired
    OrderService orderService;

    @JmsListener(destination = "PAYMENT_RESULT_QUEUE", containerFactory = "jmsQueueListener")
    public void consumerPaymentResult(MapMessage mapMessage) throws JMSException {
        String orderId = mapMessage.getString("orderId");
        String result = mapMessage.getString("result");
        if ("success".equals(result)) {
            System.out.println("订单：" + orderId + "支付成功！");
            orderService.updateOrderStatus(orderId, ProcessStatus.PAID);

            orderService.sendOrderStatus(orderId);
        } else {
            orderService.updateOrderStatus(orderId, ProcessStatus.UNPAID);
        }
    }

    @JmsListener(destination = "SKU_DEDUCT_QUEUE",containerFactory = "jmsQueueListener")
    public void consumeSkuDeduct(MapMessage mapMessage) throws JMSException {
        String orderId = mapMessage.getString("orderId");
        String  status = mapMessage.getString("status");
        if("DEDUCTED".equals(status)){
            orderService.updateOrderStatus(  orderId , ProcessStatus.WAITING_DELEVER);
        }else{
            orderService.updateOrderStatus(  orderId , ProcessStatus.STOCK_EXCEPTION);
        }
    }

}
