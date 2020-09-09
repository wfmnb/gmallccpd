package com.ccpd.gmall0822.cart.mapper;

import com.ccpd.gmall0822.bean.CartInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CartInfoMapper extends Mapper<CartInfo> {
    public List<CartInfo> getCartListByUserId(@Param("userId") String userId);

    public void mergeCart(@Param("userIdDest") String userIdDest,@Param("userIdOrig") String userIdOrig);
}
