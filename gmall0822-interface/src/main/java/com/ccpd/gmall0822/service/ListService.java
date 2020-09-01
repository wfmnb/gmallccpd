package com.ccpd.gmall0822.service;

import com.ccpd.gmall0822.bean.SkuLsInfo;
import com.ccpd.gmall0822.bean.SkuLsParams;
import com.ccpd.gmall0822.bean.SkuLsResult;

public interface ListService {

    public void saveSkuInfo(SkuLsInfo skuLsInfo);

    public SkuLsResult search(SkuLsParams skuLsParams);

    public void incrHotScore(String skuId);

}
