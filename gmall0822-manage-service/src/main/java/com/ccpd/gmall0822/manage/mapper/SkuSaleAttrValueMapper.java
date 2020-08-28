package com.ccpd.gmall0822.manage.mapper;

import com.ccpd.gmall0822.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {

    public List<Map> getSkuSaleAttrValueList(String spuId);
}
