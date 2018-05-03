package com.liujie.gmall.cart.mapper;

import com.liujie.gmall.CartInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CartInfoMapper extends Mapper<CartInfo> {
   public List<CartInfo> selectCartListWithCurPrice(Long userId);
}
