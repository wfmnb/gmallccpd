package com.ccpd.gmall0822.service;

import com.ccpd.gmall0822.bean.CartInfo;

import java.util.List;

public interface CartService {
    public CartInfo addCart(String userId,String skuId,Integer num);

    public List<CartInfo> cartList(String userId);

    public List<CartInfo> mergeCart(String userIdDest,String userIdOrig);

    public void checkCart(String isCkecked,String userId,String skuId);

    public List<CartInfo> getCheckedCartList(String userId);

}
