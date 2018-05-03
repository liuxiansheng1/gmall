package com.liujie.gmall.manageservice.mapper;

import com.liujie.gmall.SpuSaleAttr;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr>{
    public List<SpuSaleAttr> selectSpuSaleAttrList(long spuId);

    public List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(long skuId,long spuId);
}
