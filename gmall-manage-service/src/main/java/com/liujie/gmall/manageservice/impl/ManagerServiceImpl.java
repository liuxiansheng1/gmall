package com.liujie.gmall.manageservice.impl;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.liujie.gmall.*;
import com.liujie.gmall.config.RedisUtil;
import com.liujie.gmall.manageservice.constant.RedisConst;
import com.liujie.gmall.manageservice.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;

import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
@Service
public class ManagerServiceImpl implements ManageService {
    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    SpuInfoMapper spuInfoMapper;

    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    SpuImageMapper spuImageMapper;

    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    SkuInfoMapper skuInfoMapper;

    @Autowired
    SkuImageMapper skuImageMapper;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    RedisUtil redisUtil;

   public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId){
       List<SkuSaleAttrValue> skuSaleAttrValueList = skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(Long.parseLong(spuId));
    return skuSaleAttrValueList;
   }


    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo){
        List<SpuSaleAttr> saleAttrs = spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(Long.parseLong(skuInfo.getId()),
                Long.parseLong(skuInfo.getSpuId()));
        return saleAttrs;


    }

    public void saveSkuInfo(SkuInfo skuInfo){

        if(skuInfo.getId()==null||skuInfo.getId().length()==0){
            skuInfo.setId(null);
            skuInfoMapper.insertSelective(skuInfo);
        }else {
            skuInfoMapper.updateByPrimaryKeySelective(skuInfo);
        }


        Example example=new Example(SkuImage.class);
        example.createCriteria().andEqualTo("skuId",skuInfo.getId());
        skuImageMapper.deleteByExample(example);

        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage skuImage : skuImageList) {
            skuImage.setSkuId(skuInfo.getId());
            if(skuImage.getId()!=null&&skuImage.getId().length()==0) {
                skuImage.setId(null);
            }
            skuImageMapper.insertSelective(skuImage);
        }


        Example skuAttrValueExample=new Example(SkuAttrValue.class);
        skuAttrValueExample.createCriteria().andEqualTo("skuId",skuInfo.getId());
        skuAttrValueMapper.deleteByExample(skuAttrValueExample);

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            skuAttrValue.setSkuId(skuInfo.getId());
            if(skuAttrValue.getId()!=null&&skuAttrValue.getId().length()==0) {
                skuAttrValue.setId(null);
            }
            skuAttrValueMapper.insertSelective(skuAttrValue);
        }


        Example skuSaleAttrValueExample=new Example(SkuSaleAttrValue.class);
        skuSaleAttrValueExample.createCriteria().andEqualTo("skuId",skuInfo.getId());
        skuSaleAttrValueMapper.deleteByExample(skuSaleAttrValueExample);

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
            skuSaleAttrValue.setSkuId(skuInfo.getId());
            skuSaleAttrValue.setId(null);
            skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
        }

    }

    @Override
    public SkuInfo getSkuInfo(String skuId) {
        SkuInfo skuInfo = null;


        try {
            Jedis jedis = redisUtil.getJedis();
            String skuInfoKey =  RedisConst.SKU_PREFIX+skuId+RedisConst.SKU_SUFFIX;
            System.out.println(skuInfoKey);
            String skuInfoJson = jedis.get(skuInfoKey);
            if(skuInfoJson==null || skuInfoJson.length()==0){
                System.err.println(Thread.currentThread().getName()+"缓存未命中");
                String skuLockKey =  RedisConst.SKU_PREFIX+skuId+RedisConst.SKU_SUFFIX;
                String lock = jedis.set(skuLockKey, "OK", "NX", "PX", RedisConst.SKULOCK_EXPIRE_PX);
                if ("OK".equals(lock)){
                    System.err.println(Thread.currentThread().getName()+"获得分布式锁");
                    skuInfo = getSkuInfoDB(skuId);
                    if(skuInfo==null){
                        jedis.setex(skuInfoKey,RedisConst.SKUKEY_TIMEOUT,"empty");
                        return null;
                    }
                    String skuInfoJsonNew =   JSON.toJSONString(skuInfo);
                    jedis.setex(skuInfoKey,RedisConst.SKUKEY_TIMEOUT,skuInfoJsonNew);
                    jedis.close();
                    return skuInfo;
                }

            }else if ("empty".equals(skuInfoJson)){
                return null;
            }else{
                System.err.println(Thread.currentThread().getName()+"缓存命中");
                skuInfo = JSON.parseObject(skuInfoJson, SkuInfo.class);
                jedis.close();
                return skuInfo;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return getSkuInfoDB(skuId);

       /* SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> select = skuImageMapper.select(skuImage);
        skuInfo.setSkuImageList(select);
        return skuInfo;*/
    }
    public SkuInfo getSkuInfoDB(String skuId){
        System.out.println(Thread.currentThread(). getName()+"oooooooooooooooooooooo");
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        if(skuInfo!=null) {
            SkuImage skuImage = new SkuImage();
            skuImage.setSkuId(skuId);
            List<SkuImage> select = skuImageMapper.select(skuImage);
            skuInfo.setSkuImageList(select);
        }
        if(skuInfo!=null) {
            SkuAttrValue skuAttrValueQuery = new SkuAttrValue();
            skuAttrValueQuery.setSkuId(skuId);
            List<SkuAttrValue> select1 = skuAttrValueMapper.select(skuAttrValueQuery);
            skuInfo.setSkuAttrValueList(select1);
        }
        if(skuInfo!=null) {
            SkuSaleAttrValue skuAttrValueQuery = new SkuSaleAttrValue();
            skuAttrValueQuery.setSkuId(skuId);
            List<SkuSaleAttrValue> select = skuSaleAttrValueMapper.select(skuAttrValueQuery);
            skuInfo.setSkuSaleAttrValueList(select);
        }

        return skuInfo;

    }


    public void saveSpuInfo(SpuInfo spuInfo){
        if(spuInfo.getId()==null||spuInfo.getId().length()==0){
            spuInfo.setId(null);
            spuInfoMapper.insertSelective(spuInfo);
        }else{
            spuInfoMapper.updateByPrimaryKey(spuInfo);
        }

        Example spuImageExample=new Example(SpuImage.class);
        spuImageExample.createCriteria().andEqualTo("spuId",spuInfo.getId());
        spuImageMapper.deleteByExample(spuImageExample);

        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if(spuImageList!=null) {
            for (SpuImage spuImage : spuImageList) {
                if(spuImage.getId()!=null&&spuImage.getId().length()==0){
                    spuImage.setId(null);
                }
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insertSelective(spuImage);
            }
        }

        Example spuSaleAttrExample=new Example(SpuSaleAttr.class);
        spuSaleAttrExample.createCriteria().andEqualTo("spuId",spuInfo.getId());
        spuSaleAttrMapper.deleteByExample(spuSaleAttrExample);


        Example spuSaleAttrValueExample=new Example(SpuSaleAttrValue.class);
        spuSaleAttrValueExample.createCriteria().andEqualTo("spuId",spuInfo.getId());
        spuSaleAttrValueMapper.deleteByExample(spuSaleAttrValueExample);

        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if(spuSaleAttrList!=null) {
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                if(spuSaleAttr.getId()!=null&&spuSaleAttr.getId().length()==0){
                    spuSaleAttr.setId(null);
                }
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insertSelective(spuSaleAttr);
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                    if(spuSaleAttrValue.getId()!=null&&spuSaleAttrValue.getId().length()==0){
                        spuSaleAttrValue.setId(null);
                    }
                    spuSaleAttrValue.setSpuId(spuInfo.getId());
                    spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
                }
            }

        }
    }

    @Override
    public List<BaseAttrInfo> getAttrList1(String catalog3Id) {


        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.selectAttrInfoList(Long.parseLong(catalog3Id));
        return baseAttrInfoList;
    }


    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo){
        List<SpuInfo> spuInfoList = spuInfoMapper.select(spuInfo);
        return  spuInfoList;
    }

    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return  baseSaleAttrMapper.selectAll();
    }

    @Override
    public List<BaseCatalog1> getCatalog1() {
        List<BaseCatalog1> baseCatalog1List = baseCatalog1Mapper.selectAll();
        return baseCatalog1List;
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 baseCatalog2=new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);

        List<BaseCatalog2> baseCatalog2List = baseCatalog2Mapper.select(baseCatalog2);
        return baseCatalog2List;
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3=new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);

        List<BaseCatalog3> baseCatalog3List = baseCatalog3Mapper.select(baseCatalog3);
        return baseCatalog3List;
    }



    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3_id) {
        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3_id);

        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.select(baseAttrInfo);
        return baseAttrInfoList;

    }

    public List<BaseAttrInfo> getAttrList(List valueIdList) {
        String values = StringUtils.join(valueIdList, ",");
        List<BaseAttrInfo> baseAttrInfos = baseAttrInfoMapper.selectAttrInfoListByValueIds(values);
        return baseAttrInfos;
    }

    @Override
    public BaseAttrInfo getAttrInfo(String attrId) {
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(baseAttrInfo.getId());

        List<BaseAttrValue> select = baseAttrValueMapper.select(baseAttrValue);
        baseAttrInfo.setAttrValueList(select);
        return baseAttrInfo;
    }

    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //如果有主键就进行更新，如果没有就插入
        if(baseAttrInfo.getId()!=null&&baseAttrInfo.getId().length()>0){
            baseAttrInfoMapper.updateByPrimaryKey(baseAttrInfo);
        }else{
            //防止主键被赋上一个空字符串
            if(baseAttrInfo.getId().length()==0){
                baseAttrInfo.setId(null);
            }
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }
        //把原属性值全部清空
        BaseAttrValue baseAttrValue4Del = new BaseAttrValue();
        baseAttrValue4Del.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValue4Del);

        //重新插入属性
        if(baseAttrInfo.getAttrValueList()!=null&&baseAttrInfo.getAttrValueList().size()>0) {
            for (BaseAttrValue attrValue : baseAttrInfo.getAttrValueList()) {
                //防止主键被赋上一个空字符串
                if(attrValue.getId()!=null&&attrValue.getId().length()==0){
                    attrValue.setId(null);
                }
                attrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(attrValue);
            }
        }
    }

    @Override
    public List<SpuImage> getSpuImageList(String spuId){
        SpuImage spuImage=new SpuImage();
        spuImage.setSpuId(spuId);
        List<SpuImage> spuImageList = spuImageMapper.select(spuImage);
        return spuImageList;
    }

    @Override
    public  List<SpuSaleAttr> getSpuSaleAttrList(String spuId){

        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectSpuSaleAttrList(Long.parseLong(spuId));
        return spuSaleAttrList;

    }
    public List<SkuInfo> getSkuInfoListBySpu(String spuId){
        List<SkuInfo> skuInfoList = skuInfoMapper.selectSkuInfoListBySpu(Long.parseLong(spuId));
        return  skuInfoList;

    }

}
