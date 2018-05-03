package com.liujie.gmall.manageservice.mapper;

import com.liujie.gmall.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;


public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {

    public List<BaseAttrInfo> selectAttrInfoList(long catalog3Id);

    public List<BaseAttrInfo> selectAttrInfoListByValueIds(@Param("valueIds") String valueIds);
}
