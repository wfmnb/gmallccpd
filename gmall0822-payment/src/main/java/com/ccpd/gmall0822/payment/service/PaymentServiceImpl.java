package com.ccpd.gmall0822.payment.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.ccpd.gmall0822.bean.PaymentInfo;
import com.ccpd.gmall0822.enums.PaymentStatus;
import com.ccpd.gmall0822.payment.mapper.PaymentInfoMapper;
import com.ccpd.gmall0822.service.PaymentService;
import com.ccpd.gmall0822.util.ActiveMQUtil;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.Date;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Autowired
    AlipayClient alipayClient;

    @Override
    public void savPaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfo) {
        return paymentInfoMapper.selectOne(paymentInfo);
    }

    @Override
    public void updatePaymentInfoByOutTradeNo(String outTradeNo, PaymentInfo paymentInfo) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo", outTradeNo);
        paymentInfoMapper.updateByExampleSelective(paymentInfo, example);
    }

    @Override
    public void sendPaymentResult(String orderId, String result) {
        Connection connection = activeMQUtil.getConnection();
        try {
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue queue = session.createQueue("PAYMENT_RESULT_QUEUE");
            MessageProducer producer = session.createProducer(queue);
            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("orderId", orderId);
            mapMessage.setString("result", result);
            producer.send(mapMessage);
            session.commit();

            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    public void sendDelayPaymentResult(String outTradeNo, int delaySec, int checkCount) {
        //发送支付结果
        Connection connection = activeMQUtil.getConnection();
        try {
            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue paymentResultQueue = session.createQueue("PAYMENT_RESULT_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(paymentResultQueue);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("outTradeNo", outTradeNo);
            mapMessage.setInt("delaySec", delaySec);
            mapMessage.setInt("checkCount", checkCount);
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, delaySec * 1000);
            producer.send(mapMessage);

            session.commit();
            producer.close();
            session.close();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    @JmsListener(destination = "PAYMENT_RESULT_CHECK_QUEUE", containerFactory = "jmsQueueListener")
    public void consumeCheckResult(MapMessage mapMessage) throws JMSException {
        int delaySec = mapMessage.getInt("delaySec");
        String outTradeNo = mapMessage.getString("outTradeNo");
        int checkCount = mapMessage.getInt("checkCount");

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(outTradeNo);
        PaymentStatus paymentStatus = checkAlipayPayment(paymentInfo);
        if (paymentStatus == PaymentStatus.UNPAID && checkCount > 0) {
            System.out.println("checkCount = " + checkCount);
            sendDelayPaymentResult(outTradeNo, delaySec, checkCount - 1);
        }

    }

    public PaymentStatus checkAlipayPayment(PaymentInfo paymentInfo) {

        System.out.println("开始主动检查支付状态，paymentInfo.toString() = " + paymentInfo.toString());
        //先检查当前数据库是否已经变为“已支付状态”
        if (paymentInfo.getId() == null) {
            System.out.println("outTradeNo:" + paymentInfo.getOutTradeNo());
            paymentInfo = getPaymentInfo(paymentInfo);
        }
        if (paymentInfo.getPaymentStatus() == PaymentStatus.PAID) {
            System.out.println("该单据已支付:" + paymentInfo.getOutTradeNo());
            return PaymentStatus.PAID;
        }

        //如果不是已支付，继续去查询alipay的接口
        System.out.println("%% % % 查询alipay的接口");
        AlipayTradeQueryRequest alipayTradeQueryRequest = new AlipayTradeQueryRequest();
        alipayTradeQueryRequest.setBizContent("{\"out_trade_no\":\"" + paymentInfo.getOutTradeNo() + "\"}");
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(alipayTradeQueryRequest);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }


        if (response.isSuccess()) {
            String tradeStatus = response.getTradeStatus();

            if ("TRADE_SUCCESS".equals(tradeStatus)) {
                System.out.println("支付完成  ======================  ");
                //如果结果是支付成功 ,则更新支付状态
                PaymentInfo paymentInfo4Upt = new PaymentInfo();
                paymentInfo4Upt.setPaymentStatus(PaymentStatus.PAID);
                paymentInfo4Upt.setCallbackTime(new Date());
                paymentInfo4Upt.setCallbackContent(response.getBody());
                paymentInfo4Upt.setId(paymentInfo.getId());
                paymentInfoMapper.updateByPrimaryKeySelective(paymentInfo4Upt);

                // 然后发送通知给订单
                sendPaymentResult(paymentInfo.getOrderId(), "success");
                return PaymentStatus.PAID;
            } else {
                System.out.println("支付尚未完成 ？？？？？？？？？？ ");
                return PaymentStatus.UNPAID;
            }
        } else {
            System.out.println("支付尚未完成 ？？？？？？？？？？ ");
            return PaymentStatus.UNPAID;
        }


    }


}
