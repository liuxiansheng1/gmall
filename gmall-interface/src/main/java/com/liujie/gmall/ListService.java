package com.liujie.gmall;

public interface ListService {
    public void saveSkuInfo(SkuLsInfo skuInfo);

    public SkuLsResult searchSkuinfoList(SkuLsParams skuLsParams);

    public void incrHotScore(String skuId);
}
