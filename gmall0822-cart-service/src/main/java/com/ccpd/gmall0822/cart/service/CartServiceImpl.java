package com.ccpd.gmall0822.cart.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.ccpd.gmall0822.bean.CartInfo;
import com.ccpd.gmall0822.bean.SkuInfo;
import com.ccpd.gmall0822.cart.mapper.CartInfoMapper;
import com.ccpd.gmall0822.service.CartService;
import com.ccpd.gmall0822.service.ManageService;
import com.ccpd.gmall0822.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    CartInfoMapper cartInfoMapper;

    @Autowired
    RedisUtil redisUtil;

    @Reference
    ManageService manageService;

    public static final String CARTKEY_PREFIX = "cart:";
    public static final String CARTKEY_SUFFIX = ":info";
    public static final String CARTCHECKEDKEY_SUFFIX = ":ckecked";
    public static final int TIME_OUT = 60 * 60 * 24;

    @Override
    public CartInfo addCart(String userId, String skuId, Integer num) {
        //写入数据库
        CartInfo cartInfoQuery = new CartInfo();
        cartInfoQuery.setUserId(userId);
        cartInfoQuery.setSkuId(skuId);
        CartInfo cartInfoExists = cartInfoMapper.selectOne(cartInfoQuery);
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        if (cartInfoExists != null) {
            cartInfoExists.setSkuName(skuInfo.getSkuName());
            cartInfoExists.setSkuPrice(skuInfo.getPrice());
            cartInfoExists.setSkuNum(cartInfoExists.getSkuNum() + num);
            cartInfoExists.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExists);
        } else {
            CartInfo cartInfo = new CartInfo();
            cartInfo.setSkuId(skuId);
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuNum(num);
            cartInfo.setUserId(userId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfoMapper.insertSelective(cartInfo);
            cartInfoExists = cartInfo;
        }

        //写入缓存
        Jedis jedis = redisUtil.getJedis();
        String cartKey = CARTKEY_PREFIX + userId + CARTKEY_SUFFIX;
        jedis.hset(cartKey, skuId, JSON.toJSONString(cartInfoExists));
        jedis.expire(cartKey, TIME_OUT);
        jedis.close();
        return cartInfoExists;
    }

    @Override
    public List<CartInfo> cartList(String userId) {
        //查缓存
        Jedis jedis = redisUtil.getJedis();
        String cartKey = CARTKEY_PREFIX + userId + CARTKEY_SUFFIX;
        List<String> cartInfoJsonList = jedis.hvals(cartKey);
        List<CartInfo> cartInfos = new ArrayList<>();
        if (cartInfoJsonList != null && cartInfoJsonList.size() > 0) {
            for (String cartInfoJson : cartInfoJsonList) {
                CartInfo cartInfo = JSON.parseObject(cartInfoJson, CartInfo.class);
                cartInfos.add(cartInfo);
            }
            return cartInfos;
        }
        jedis.close();
        return loadCache(userId);
    }

    public List<CartInfo> loadCache(String userId) {
        //未命中缓存，查询数据库
        Jedis jedis = redisUtil.getJedis();
        String cartKey = CARTKEY_PREFIX + userId + CARTKEY_SUFFIX;
        List<CartInfo> cartInfoList = cartInfoMapper.getCartListByUserId(userId);
        if (cartInfoList == null) {
            return null;
        }
        //写入缓存
        Map<String, String> map = new HashMap<>();
        for (CartInfo cartInfo : cartInfoList) {
            map.put(cartInfo.getSkuId(), JSON.toJSONString(cartInfo));
        }
        jedis.del(cartKey);
        jedis.hmset(cartKey, map);
        jedis.expire(cartKey, TIME_OUT);
        jedis.close();
        return cartInfoList;
    }

    @Override
    public List<CartInfo> mergeCart(String userIdDest, String userIdOrig) {
        cartInfoMapper.mergeCart(userIdDest, userIdOrig);
        List<CartInfo> cartInfos = loadCache(userIdOrig);
        CartInfo cartInfoDel = new CartInfo();
        cartInfoDel.setUserId(userIdDest);
        cartInfoMapper.delete(cartInfoDel);
        return cartInfos;
    }

    public void cacheExists(String userId, String skuId) {
        String cartKey = CARTKEY_PREFIX + userId + CARTKEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        Long ttl = jedis.ttl(cartKey);
        jedis.expire(cartKey, ttl.intValue() + 10);
        Boolean exists = jedis.hexists(cartKey, skuId);
        if (!exists) {
            loadCache(userId);
        }
        jedis.close();
    }

    @Override
    public void checkCart(String isCkecked, String userId, String skuId) {
        Jedis jedis = redisUtil.getJedis();
        String cartKey = CARTKEY_PREFIX + userId + CARTKEY_SUFFIX;
        //判断缓存存不存在，不存在则加载缓存
        cacheExists(userId, skuId);
        String cartInfoJson = jedis.hget(cartKey, skuId);
        CartInfo cartInfo = JSON.parseObject(cartInfoJson, CartInfo.class);
        cartInfo.setIsChecked(isCkecked);
        jedis.hset(cartKey, skuId, JSON.toJSONString(cartInfo));

        //将选中的商品，单独存在缓存中
        String cartCheckedKey = CARTKEY_PREFIX + userId + CARTCHECKEDKEY_SUFFIX;
        if ("1".equals(isCkecked)) {
            jedis.hset(cartCheckedKey, skuId, JSON.toJSONString(cartInfo));
            jedis.expire(cartCheckedKey, TIME_OUT);
        } else {
            jedis.hdel(cartCheckedKey, skuId);
        }
        jedis.close();
    }

    @Override
    public List<CartInfo> getCheckedCartList(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String cartCheckedKey = CARTKEY_PREFIX + userId + CARTCHECKEDKEY_SUFFIX;
        List<String> cartInfoJsonList = jedis.hvals(cartCheckedKey);
        List<CartInfo> cartInfoList = new ArrayList<>();
        if (cartInfoJsonList == null || cartInfoJsonList.size() == 0) {
            return null;
        }
        for (String cartInfoJson : cartInfoJsonList) {
            CartInfo cartInfo = JSON.parseObject(cartInfoJson, CartInfo.class);
            cartInfoList.add(cartInfo);
        }
        jedis.close();
        return cartInfoList;
    }

}
