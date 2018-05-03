package com.liujie.gmall;

import java.util.List;

public interface ManageService {

    public List<BaseCatalog1> getCatalog1();

    public List<BaseCatalog2> getCatalog2(String catalog1Id);

    public List<BaseCatalog3> getCatalog3(String catalog2Id);

    public List<BaseAttrInfo> getAttrList(String catalog2Id);

    public List<BaseAttrInfo> getAttrList(List valueIds);

    BaseAttrInfo getAttrInfo(String attrId);

    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo);

    public List<BaseSaleAttr> getBaseSaleAttrList();

    public void saveSpuInfo(SpuInfo spuInfo);

    public List<BaseAttrInfo> getAttrList1(String catalog3Id);

    public List<SpuImage> getSpuImageList(String spuId);

    public  List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    public void saveSkuInfo(SkuInfo skuInfo);

    public SkuInfo getSkuInfo(String SkuId);

    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo);

    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);

    public List<SkuInfo> getSkuInfoListBySpu(String spuId);

    public SkuInfo getSkuInfoDB(String skuId);




}
