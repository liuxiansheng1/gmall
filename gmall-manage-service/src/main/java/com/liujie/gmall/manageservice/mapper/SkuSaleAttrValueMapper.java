package com.liujie.gmall.manageservice.mapper;

import com.liujie.gmall.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {

   public List<SkuSaleAttrValue> selectSkuSaleAttrValueListBySpu(Long spuId);
}
