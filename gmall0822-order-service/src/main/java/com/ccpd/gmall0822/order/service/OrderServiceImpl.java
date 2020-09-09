package com.ccpd.gmall0822.order.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.ccpd.gmall0822.bean.OrderDetail;
import com.ccpd.gmall0822.bean.OrderInfo;
import com.ccpd.gmall0822.order.mapper.OrderDetailMapper;
import com.ccpd.gmall0822.order.mapper.OrderInfoMapper;
import com.ccpd.gmall0822.service.OrderService;
import com.ccpd.gmall0822.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Autowired
    RedisUtil redisUtil;

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
        jedis.setex(tradeNoKey,TRADENO_TIME_OUT,tradeNo);
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
           if(tradeNoForRedis != null && tradeNo != null && tradeNoForRedis.equals(tradeNo)){
               transaction.del(tradeNoKey);
           }
           List<Object> list = transaction.exec();
           if(list != null && list.size() > 0 && (long)list.get(0) == 1L){
               bool = true;
           }
           return bool;
       }finally {
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

}
