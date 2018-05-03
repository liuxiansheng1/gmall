package com.liujie.gmall.manageservice.mapper;

import com.liujie.gmall.SkuInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuInfoMapper extends Mapper<SkuInfo> {


    List<SkuInfo> selectSkuInfoListBySpu(long spuId);
}
