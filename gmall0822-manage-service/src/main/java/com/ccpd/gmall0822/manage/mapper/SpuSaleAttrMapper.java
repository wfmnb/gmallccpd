package com.ccpd.gmall0822.manage.mapper;

import com.ccpd.gmall0822.bean.SpuSaleAttr;
import com.ccpd.gmall0822.bean.SpuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {
    public List<SpuSaleAttr> getSpuSaleAttrValueListAndCheck(String skuId, String spuId);
}
