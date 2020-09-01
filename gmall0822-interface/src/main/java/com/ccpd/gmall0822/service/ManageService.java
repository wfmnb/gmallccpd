package com.ccpd.gmall0822.service;

import com.ccpd.gmall0822.bean.*;

import java.util.List;
import java.util.Map;

public interface ManageService {
    public List<BaseCatalog1> getCatalog1();

    public List<BaseCatalog2> getCatalog2(String catalog1Id);

    public List<BaseCatalog3> getCatalog3(String catalog2Id);

    public List<BaseAttrInfo> getAttrList(String catalog3Id);

    public void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    public BaseAttrInfo getAttrInfo(String attrId);

    public List<SpuInfo> getSpuInfoList(String catalog3Id);

    public List<BaseSaleAttr> getBaseSaleAttrList();

    public void saveSpuInfo(SpuInfo spuInfo);

    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    public List<SpuImage> getSpuImageList(String spuId);

    public void saveSkuInfo(SkuInfo skuInfo);

    public SkuInfo getSkuInfo(String skuId);

    public List<SpuSaleAttr> getSpuSaleAttrListBySpuIdChecked(String skuId, String spuId);

    public Map getSkuValueIdsMap(String spuId);

    public List<BaseAttrInfo> getAttrList(List<String> attrValueIdList);
}
