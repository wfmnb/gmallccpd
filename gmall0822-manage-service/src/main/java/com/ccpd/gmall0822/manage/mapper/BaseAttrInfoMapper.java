package com.ccpd.gmall0822.manage.mapper;

import com.ccpd.gmall0822.bean.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {
    public List<BaseAttrInfo> getBaseAttrInfoListByCatalog3Id(String catalog3Id);
    public List<BaseAttrInfo> selectAttrInfoListByIds(@Param("valueIds") String valueIds);

}
