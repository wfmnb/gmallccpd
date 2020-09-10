package com.ccpd.gmall0822.order.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.ccpd.gmall0822.bean.OrderDetail;
import com.ccpd.gmall0822.bean.OrderInfo;
import com.ccpd.gmall0822.enums.ProcessStatus;
import com.ccpd.gmall0822.order.mapper.OrderDetailMapper;
import com.ccpd.gmall0822.order.mapper.OrderInfoMapper;
import com.ccpd.gmall0822.service.OrderService;
import com.ccpd.gmall0822.util.ActiveMQUtil;
import com.ccpd.gmall0822.util.RedisUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import javax.jms.*;
import javax.jms.Queue;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    ActiveMQUtil activeMQUtil;

    public static final String ORDERKEY_PREFIX = "order:";
    public static final String TRADENOKEY_SUFFIX = ":tradeNo";
    public static final int TRADENO_TIME_OUT = 60 * 10;

    @Override
    @Transactional
    public String saveOrder(OrderInfo orderInfo) {
        orderInfoMapper.insertSelective(orderInfo);
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }
        return orderInfo.getId();
    }

    @Override
    public String genTradeNo(String userId) {
        String tradeNoKey = ORDERKEY_PREFIX + userId + TRADENOKEY_SUFFIX;
        String tradeNo = UUID.randomUUID().toString();
        Jedis jedis = redisUtil.getJedis();
        jedis.setex(tradeNoKey, TRADENO_TIME_OUT, tradeNo);
        jedis.close();
        return tradeNo;
    }

    @Override
    public boolean verifyTradeNo(String userId, String tradeNo) {
        String tradeNoKey = ORDERKEY_PREFIX + userId + TRADENOKEY_SUFFIX;
        boolean bool = false;
        Jedis jedis = redisUtil.getJedis();
        try {
            String tradeNoForRedis = jedis.get(tradeNoKey);
            jedis.watch(tradeNoKey);
            Transaction transaction = jedis.multi();
            if (tradeNoForRedis != null && tradeNo != null && tradeNoForRedis.equals(tradeNo)) {
                transaction.del(tradeNoKey);
            }
            List<Object> list = transaction.exec();
            if (list != null && list.size() > 0 && (long) list.get(0) == 1L) {
                bool = true;
            }
            return bool;
        } finally {
            jedis.close();
        }
    }

    @Override
    public OrderInfo getOrderInfoByOrderId(String orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);
        orderInfo.setOrderDetailList(orderDetailList);
        return orderInfo;
    }

    @Override
    public void updateOrderStatus(String orderId, ProcessStatus processStatus) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setProcessStatus(processStatus);
        orderInfo.setOrderStatus(processStatus.getOrderStatus());
        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);
    }

    @Override
    public void sendOrderStatus(String orderId) {
        Connection connection = activeMQUtil.getConnection();
        String orderJson = initWareOrder(orderId);
        try {
            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue order_result_queue = session.createQueue("ORDER_RESULT_QUEUE");
            MessageProducer producer = session.createProducer(order_result_queue);

            ActiveMQTextMessage textMessage = new ActiveMQTextMessage();
            textMessage.setText(orderJson);
            producer.send(textMessage);
            session.commit();
            session.close();
            producer.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public String initWareOrder(String orderId) {
        OrderInfo orderInfo = getOrderInfoByOrderId(orderId);
        Map map = initWareOrder(orderInfo);
        return JSON.toJSONString(map);
    }

    // 设置初始化仓库信息方法
    public Map initWareOrder(OrderInfo orderInfo) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", orderInfo.getId());
        map.put("consignee", orderInfo.getConsignee());
        map.put("consigneeTel", orderInfo.getConsigneeTel());
        map.put("orderComment", orderInfo.getOrderComment());
        map.put("orderBody", orderInfo.getTradeBody());
        map.put("deliveryAddress", orderInfo.getDeliveryAddress());
        map.put("paymentWay", "2");
        map.put("wareId", orderInfo.getWareId());

        // 组合json
        List detailList = new ArrayList();
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            Map detailMap = new HashMap();
            detailMap.put("skuId", orderDetail.getSkuId());
            detailMap.put("skuName", orderDetail.getSkuName());
            detailMap.put("skuNum", orderDetail.getSkuNum());
            detailList.add(detailMap);
        }
        map.put("details", detailList);
        return map;
    }

    public List<Integer> checkExpiredCoupon() {
        return Arrays.asList(1, 2, 3, 4, 5, 6, 7);
    }

    @Async
    public void handleExpiredCoupon(Integer id) {
        try {
            System.out.println("购物券" + id + "发送用户");
            Thread.sleep(1000);

            System.out.println("购物券" + id + "删除");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Map> orderSplit(String orderId, String wareSkuMapJson) {

        List<Map> wareParamMapList = new ArrayList<>();
        //查出原始订单
        OrderInfo orderInfo = getOrderInfoByOrderId(orderId);

        //wareSkuMap => List
        List<Map> mapList = JSON.parseArray(wareSkuMapJson, Map.class);

        //循环生成子订单
        for (Map wareSkuMap : mapList) {
            OrderInfo orderInfoSub = new OrderInfo();
            try {
                BeanUtils.copyProperties(orderInfoSub, orderInfo);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            List<String> skuIds = (List<String>) wareSkuMap.get("skuIds");
            List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
            ArrayList<OrderDetail> orderDetailSubList = new ArrayList<>();
            for (String skuId : skuIds) {
                for (OrderDetail orderDetail : orderDetailList) {
                    if (skuId.equals(orderDetail.getSkuId())) {
                        OrderDetail orderDetailSub = new OrderDetail();
                        try {
                            BeanUtils.copyProperties(orderDetailSub, orderDetail);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                        orderDetailSub.setId(null);
                        orderDetailSub.setOrderId(null);
                        orderDetailSubList.add(orderDetailSub);
                    }
                }
            }
            orderInfoSub.setOrderDetailList(orderDetailSubList);
            orderInfoSub.setId(null);
            orderInfoSub.sumTotalAmount();
            orderInfoSub.setParentOrderId(orderId);

            saveOrder(orderInfoSub);

            Map wareParamMap = initWareOrder(orderInfoSub);
            wareParamMap.put("wareId", wareSkuMap.get("wareId"));

            wareParamMapList.add(wareParamMap);
        }
        updateOrderStatus(orderId,ProcessStatus.SPLIT);
        return wareParamMapList;
    }
}
